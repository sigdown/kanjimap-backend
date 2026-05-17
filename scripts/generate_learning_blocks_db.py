#!/usr/bin/env python3
from __future__ import annotations

import argparse
import sys
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

from psycopg2.extras import execute_batch

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from load_dictionary_seed_env import connect, load_db_config


@dataclass(frozen=True)
class WordRow:
    word_id: int
    writing_form: str
    reading_kana: str
    jlpt_level: str | None
    topic_name: str | None


@dataclass(frozen=True)
class KanjiLinkRow:
    kanji_id: int
    kanji: str
    position_index: int | None


@dataclass(frozen=True)
class BlockSpec:
    title: str
    description: str
    topics: tuple[str, ...]


DEFAULT_BLOCK_SPECS: tuple[BlockSpec, ...] = (
    BlockSpec(
        title="N5: числа и деньги",
        description="Базовые числа, деньги и первые простые количественные выражения.",
        topics=("numbers", "money"),
    ),
    BlockSpec(
        title="N5: природа и мир вокруг",
        description="Природа, базовые объекты мира вокруг и несколько частых простых существительных.",
        topics=("nature", "food", "places"),
    ),
    BlockSpec(
        title="N5: люди и учёба",
        description="Люди, школа, язык и базовая учебная лексика.",
        topics=("people", "school", "language"),
    ),
    BlockSpec(
        title="N5: положение и признаки",
        description="Положение в пространстве, направления и базовые прилагательные.",
        topics=("position", "adjectives", "body"),
    ),
    BlockSpec(
        title="N5: действия каждый день",
        description="Повседневные глаголы, время и несколько базовых слов для простых фраз.",
        topics=("verbs", "time", "objects"),
    ),
)


def fetch_words(conn, jlpt_level: str | None) -> list[WordRow]:
    where_sql = ""
    params: tuple[object, ...] = ()
    if jlpt_level:
        where_sql = "WHERE jlpt_level = %s"
        params = (jlpt_level,)

    with conn.cursor() as cur:
        cur.execute(
            f"""
            SELECT word_id, writing_form, reading_kana, jlpt_level, topic_name
            FROM public.word
            {where_sql}
            ORDER BY word_id
            """,
            params,
        )
        return [WordRow(*row) for row in cur.fetchall()]


def fetch_kanji_links(conn, word_ids: set[int]) -> dict[int, list[KanjiLinkRow]]:
    if not word_ids:
        return {}

    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT kw.word_id, kw.kanji_id, k.kanji, kw.position_index
            FROM public.kanji_word kw
            INNER JOIN public.kanji k ON k.kanji_id = kw.kanji_id
            ORDER BY kw.word_id, COALESCE(kw.position_index, 0), kw.kanji_id
            """
        )
        result: dict[int, list[KanjiLinkRow]] = defaultdict(list)
        for word_id, kanji_id, kanji, position_index in cur.fetchall():
            if word_id in word_ids:
                result[word_id].append(KanjiLinkRow(kanji_id, kanji, position_index))
        return result


def build_blocks(words: list[WordRow], kanji_links: dict[int, list[KanjiLinkRow]]) -> list[tuple[BlockSpec, list[WordRow], list[KanjiLinkRow]]]:
    words_by_topic: dict[str, list[WordRow]] = defaultdict(list)
    for word in words:
        topic = (word.topic_name or "").strip()
        if topic:
            words_by_topic[topic].append(word)

    generated: list[tuple[BlockSpec, list[WordRow], list[KanjiLinkRow]]] = []

    for spec in DEFAULT_BLOCK_SPECS:
        selected_words: list[WordRow] = []
        seen_word_ids: set[int] = set()

        for topic in spec.topics:
            for word in words_by_topic.get(topic, []):
                if word.word_id in seen_word_ids:
                    continue
                selected_words.append(word)
                seen_word_ids.add(word.word_id)

        kanji_by_id: dict[int, KanjiLinkRow] = {}
        for word in selected_words:
            for link in kanji_links.get(word.word_id, []):
                kanji_by_id.setdefault(link.kanji_id, link)

        selected_kanji: list[KanjiLinkRow] = list(kanji_by_id.values())
        generated.append((spec, selected_words, selected_kanji))

    return generated


def find_existing_block_id(cur, title: str) -> int | None:
    cur.execute(
        """
        SELECT learning_block_id
        FROM public.learning_block
        WHERE title = %s
        ORDER BY learning_block_id
        """,
        (title,),
    )
    rows = cur.fetchall()
    if len(rows) > 1:
        raise RuntimeError(
            f"Found multiple learning_block rows with title '{title}'. "
            "Cleanup is required before deterministic upsert."
        )
    return rows[0][0] if rows else None


def next_order_index(cur) -> int:
    cur.execute("SELECT COALESCE(MAX(order_index), 0) FROM public.learning_block")
    return int(cur.fetchone()[0]) + 1


def upsert_block(cur, spec: BlockSpec, order_index: int) -> int:
    existing_id = find_existing_block_id(cur, spec.title)
    if existing_id is None:
        cur.execute(
            """
            INSERT INTO public.learning_block (title, description, block_type, order_index)
            VALUES (%s, %s, 'mixed', %s)
            RETURNING learning_block_id
            """,
            (spec.title, spec.description, order_index),
        )
        return int(cur.fetchone()[0])

    cur.execute(
        """
        UPDATE public.learning_block
        SET description = %s,
            block_type = 'mixed',
            order_index = %s
        WHERE learning_block_id = %s
        """,
        (spec.description, order_index, existing_id),
    )
    return existing_id


def replace_block_content(cur, block_id: int, words: list[WordRow], kanji: list[KanjiLinkRow]) -> None:
    cur.execute(
        "DELETE FROM public.learning_block_word WHERE learning_block_id = %s",
        (block_id,),
    )
    cur.execute(
        "DELETE FROM public.learning_block_kanji WHERE learning_block_id = %s",
        (block_id,),
    )

    execute_batch(
        cur,
        """
        INSERT INTO public.learning_block_word (learning_block_id, word_id, order_index)
        VALUES (%s, %s, %s)
        """,
        [
            (block_id, word.word_id, index)
            for index, word in enumerate(words, start=1)
        ],
    )

    execute_batch(
        cur,
        """
        INSERT INTO public.learning_block_kanji (learning_block_id, kanji_id, order_index)
        VALUES (%s, %s, %s)
        """,
        [
            (block_id, link.kanji_id, index)
            for index, link in enumerate(kanji, start=1)
        ],
    )


def generate_learning_blocks(env_path: Path, jlpt_level: str | None, dry_run: bool) -> None:
    config = load_db_config(env_path)
    try:
        conn = connect(config)
    except Exception as error:
        message = str(error).strip() or repr(error)
        raise RuntimeError(
            f"Failed to connect to PostgreSQL at {config.host}:{config.port}/{config.database} "
            f"as {config.user}: {message}"
        ) from error

    try:
        with conn:
            words = fetch_words(conn, jlpt_level)
            if not words:
                raise RuntimeError("No words found for the selected filter. Cannot generate learning blocks.")

            kanji_links = fetch_kanji_links(conn, {word.word_id for word in words})
            generated = build_blocks(words, kanji_links)

            with conn.cursor() as cur:
                order_index = next_order_index(cur)

                for spec, block_words, block_kanji in generated:
                    if not block_words:
                        print(f"[skip] {spec.title}: no words matched topics {', '.join(spec.topics)}")
                        continue

                    print(
                        f"[plan] {spec.title}: "
                        f"{len(block_words)} words, {len(block_kanji)} kanji, "
                        f"topics={', '.join(spec.topics)}"
                    )

                    if dry_run:
                        continue

                    block_id = upsert_block(cur, spec, order_index)
                    replace_block_content(cur, block_id, block_words, block_kanji)
                    print(f"[ok] block_id={block_id} title={spec.title}")
                    order_index += 1

            if dry_run:
                conn.rollback()
                print("[done] dry-run completed, no DB changes were committed")
            else:
                print("[done] learning blocks generated in DB")

    finally:
        conn.close()


def parse_args(argv: Iterable[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate learning blocks in DB from real word/kanji seed data already loaded into tables."
    )
    parser.add_argument(
        "--env",
        default=".env",
        help="Path to .env file. Default: .env",
    )
    parser.add_argument(
        "--jlpt-level",
        default="N5",
        help="JLPT level filter for source words. Use empty string to disable filtering. Default: N5",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print the generation plan without committing changes.",
    )
    return parser.parse_args(list(argv))


def main(argv: Iterable[str]) -> int:
    args = parse_args(argv)
    env_path = (Path.cwd() / args.env).resolve()
    jlpt_level = args.jlpt_level.strip() or None

    try:
        generate_learning_blocks(
            env_path=env_path,
            jlpt_level=jlpt_level,
            dry_run=args.dry_run,
        )
        return 0
    except Exception as error:
        message = str(error).strip() or repr(error)
        print(f"[error] {message}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
