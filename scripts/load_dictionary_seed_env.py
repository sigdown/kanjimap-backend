#!/usr/bin/env python3
"""
Seed loader for KanjiMap dictionary tables.

Reads DB config from .env in the project root:

SERVER_HOST=0.0.0.0
SERVER_PORT=8080
DATABASE_HOST=postgres
DATABASE_PORT=5432
DATABASE_NAME=app
DATABASE_USER=app
DATABASE_PASSWORD=app
DATABASE_MAX_POOL_SIZE=10

Expected CSV files by default:
  seed-data/kanji.csv
  seed-data/kanji_reading.csv
  seed-data/kanji_meaning.csv
  seed-data/word.csv
  seed-data/word_meaning.csv
  seed-data/kanji_word.csv
  seed-data/learning_block.csv
  seed-data/learning_block_word.csv
  seed-data/learning_block_kanji.csv
  seed-data/word_relation.csv

Usage:
  pip install psycopg2-binary
  python scripts/load_dictionary_seed.py

Optional:
  python scripts/load_dictionary_seed.py --env .env --csv-dir seed-data
"""

from __future__ import annotations

import argparse
import csv
import os
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Optional

import psycopg2
from psycopg2.extras import execute_batch


@dataclass(frozen=True)
class DbConfig:
    host: str
    port: int
    database: str
    user: str
    password: str


def read_env_file(path: Path) -> dict[str, str]:
    if not path.exists():
        raise FileNotFoundError(f".env file not found: {path}")

    env: dict[str, str] = {}

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()

        if not line or line.startswith("#"):
            continue

        if "=" not in line:
            continue

        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip().strip('"').strip("'")
        env[key] = value

    return env


def load_db_config(env_path: Path) -> DbConfig:
    file_env = read_env_file(env_path)

    # OS environment wins over .env, which is useful in CI or Docker.
    merged_env = {**file_env, **os.environ}

    host = merged_env.get("DATABASE_HOST")
    port = merged_env.get("DATABASE_PORT")
    database = merged_env.get("DATABASE_NAME")
    user = merged_env.get("DATABASE_USER")
    password = merged_env.get("DATABASE_PASSWORD")

    missing = [
        name
        for name, value in {
            "DATABASE_HOST": host,
            "DATABASE_PORT": port,
            "DATABASE_NAME": database,
            "DATABASE_USER": user,
            "DATABASE_PASSWORD": password,
        }.items()
        if not value
    ]

    if missing:
        raise RuntimeError(f"Missing required env values: {', '.join(missing)}")

    return DbConfig(
        host=host,
        port=int(port),
        database=database,
        user=user,
        password=password,
    )


def read_csv(path: Path) -> list[dict[str, str]]:
    if not path.exists():
        print(f"[skip] {path} not found")
        return []

    with path.open("r", encoding="utf-8-sig", newline="") as file:
        return list(csv.DictReader(file))


def optional_int(value: Optional[str]) -> Optional[int]:
    if value is None or value == "":
        return None
    return int(value)


def optional_str(value: Optional[str]) -> Optional[str]:
    if value is None or value == "":
        return None
    return value


def get_required(row: dict[str, str], *column_names: str) -> str:
    """
    Returns the first existing non-empty value from possible CSV column names.

    This keeps the loader tolerant to headers like:
      title
      learning_block_title
      block_title
    """
    for column_name in column_names:
        value = row.get(column_name)
        if value is not None and value != "":
            return value

    available = ", ".join(row.keys())
    expected = " or ".join(column_names)
    raise KeyError(f"Missing required CSV column: {expected}. Available columns: {available}")


def ensure_csv_dir(csv_dir: Path) -> None:
    if not csv_dir.exists():
        raise FileNotFoundError(f"CSV directory not found: {csv_dir}")


def insert_kanji(conn, rows: list[dict[str, str]]) -> dict[str, int]:
    if not rows:
        return {}

    with conn.cursor() as cur:
        execute_batch(
            cur,
            """
            INSERT INTO public.kanji (kanji, stroke_count, jlpt_level)
            VALUES (%(kanji)s, %(stroke_count)s, %(jlpt_level)s)
                ON CONFLICT (kanji) DO UPDATE SET
                stroke_count = EXCLUDED.stroke_count,
                                           jlpt_level = EXCLUDED.jlpt_level
            """,
            [
                {
                    "kanji": row["kanji"],
                    "stroke_count": optional_int(row.get("stroke_count")),
                    "jlpt_level": optional_str(row.get("jlpt_level")),
                }
                for row in rows
            ],
        )

        cur.execute("SELECT kanji_id, kanji FROM public.kanji")
        result = {kanji: kanji_id for kanji_id, kanji in cur.fetchall()}

    print(f"[ok] kanji: {len(rows)} rows")
    return result


def insert_kanji_readings(conn, rows: list[dict[str, str]], kanji_ids: dict[str, int]) -> None:
    if not rows:
        return

    payload = []
    for row in rows:
        kanji = row["kanji"]
        kanji_id = kanji_ids.get(kanji)
        if not kanji_id:
            raise KeyError(f"Unknown kanji in kanji_reading.csv: {kanji}")

        payload.append(
            {
                "kanji_id": kanji_id,
                "reading": row["reading"],
                "reading_type": row["reading_type"],
            }
        )

    with conn.cursor() as cur:
        execute_batch(
            cur,
            """
            INSERT INTO public.kanji_reading (kanji_id, reading, reading_type)
            VALUES (%(kanji_id)s, %(reading)s, %(reading_type)s)
                ON CONFLICT (kanji_id, reading, reading_type) DO NOTHING
            """,
            payload,
        )

    print(f"[ok] kanji_reading: {len(payload)} rows")


def insert_kanji_meanings(conn, rows: list[dict[str, str]], kanji_ids: dict[str, int]) -> None:
    if not rows:
        return

    payload = []
    for row in rows:
        kanji = row["kanji"]
        kanji_id = kanji_ids.get(kanji)
        if not kanji_id:
            raise KeyError(f"Unknown kanji in kanji_meaning.csv: {kanji}")

        payload.append(
            {
                "kanji_id": kanji_id,
                "language_code": row.get("language_code") or "rus",
                "meaning": row["meaning"],
                "example": optional_str(row.get("example")),
            }
        )

    with conn.cursor() as cur:
        execute_batch(
            cur,
            """
            INSERT INTO public.kanji_meaning (kanji_id, language_code, meaning, example)
            VALUES (%(kanji_id)s, %(language_code)s, %(meaning)s, %(example)s)
                ON CONFLICT (kanji_id, language_code, meaning) DO UPDATE SET
                example = EXCLUDED.example
            """,
            payload,
        )

    print(f"[ok] kanji_meaning: {len(payload)} rows")


def insert_words(conn, rows: list[dict[str, str]]) -> dict[tuple[str, str], int]:
    if not rows:
        return {}

    payload = [
        {
            "writing_form": row["writing_form"],
            "reading_kana": row["reading_kana"],
            "jlpt_level": optional_str(row.get("jlpt_level")),
            "topic_name": optional_str(row.get("topic_name")),
        }
        for row in rows
    ]

    with conn.cursor() as cur:
        execute_batch(
            cur,
            """
            INSERT INTO public.word (writing_form, reading_kana, jlpt_level, topic_name)
            VALUES (%(writing_form)s, %(reading_kana)s, %(jlpt_level)s, %(topic_name)s)
                ON CONFLICT (writing_form, reading_kana) DO UPDATE SET
                jlpt_level = EXCLUDED.jlpt_level,
                                                                topic_name = EXCLUDED.topic_name
            """,
            payload,
        )

        cur.execute("SELECT word_id, writing_form, reading_kana FROM public.word")
        result = {
            (writing_form, reading_kana): word_id
            for word_id, writing_form, reading_kana in cur.fetchall()
        }

    print(f"[ok] word: {len(rows)} rows")
    return result


def insert_word_meanings(
        conn,
        rows: list[dict[str, str]],
        word_ids: dict[tuple[str, str], int],
) -> None:
    if not rows:
        return

    payload = []
    for row in rows:
        key = (row["writing_form"], row["reading_kana"])
        word_id = word_ids.get(key)
        if not word_id:
            raise KeyError(f"Unknown word in word_meaning.csv: {key}")

        payload.append(
            {
                "word_id": word_id,
                "meaning": row["meaning"],
                "example_jp": optional_str(row.get("example_jp")),
                "example_translation": optional_str(row.get("example_translation")),
                "part_of_speech": optional_str(row.get("part_of_speech")),
            }
        )

    # word_meaning has no unique constraint in the current DDL.
    # So we avoid duplicates manually instead of pretending the schema protects us.
    with conn.cursor() as cur:
        execute_batch(
            cur,
            """
            INSERT INTO public.word_meaning
                (word_id, meaning, example_jp, example_translation, part_of_speech)
            SELECT
                %(word_id)s,
                %(meaning)s,
                %(example_jp)s,
                %(example_translation)s,
                %(part_of_speech)s
                WHERE NOT EXISTS (
                SELECT 1
                FROM public.word_meaning wm
                WHERE wm.word_id = %(word_id)s
                AND wm.meaning = %(meaning)s
                AND COALESCE(wm.part_of_speech, '') = COALESCE(%(part_of_speech)s, '')
                )
            """,
            payload,
        )

    print(f"[ok] word_meaning: {len(payload)} rows")


def insert_kanji_word(
        conn,
        rows: list[dict[str, str]],
        kanji_ids: dict[str, int],
        word_ids: dict[tuple[str, str], int],
) -> None:
    if not rows:
        return

    payload = []

    for row in rows:
        kanji = row["kanji"]
        key = (row["writing_form"], row["reading_kana"])

        kanji_id = kanji_ids.get(kanji)
        word_id = word_ids.get(key)

        if not kanji_id:
            raise KeyError(f"Unknown kanji in kanji_word.csv: {kanji}")

        if not word_id:
            raise KeyError(f"Unknown word in kanji_word.csv: {key}")

        payload.append(
            {
                "kanji_id": kanji_id,
                "word_id": word_id,
                "position_index": optional_int(row.get("position_index")),
            }
        )

    with conn.cursor() as cur:
        execute_batch(
            cur,
            """
            INSERT INTO public.kanji_word (kanji_id, word_id, position_index)
            VALUES (%(kanji_id)s, %(word_id)s, %(position_index)s)
                ON CONFLICT (kanji_id, word_id) DO UPDATE SET
                position_index = EXCLUDED.position_index
            """,
            payload,
        )

    print(f"[ok] kanji_word: {len(payload)} rows")


def insert_learning_blocks(conn, rows: list[dict[str, str]]) -> dict[str, int]:
    if not rows:
        return {}

    payload = [
        {
            "title": row["title"],
            "description": optional_str(row.get("description")),
            "block_type": row.get("block_type") or "mixed",
            "order_index": optional_int(row.get("order_index")) or 0,
        }
        for row in rows
    ]

    # learning_block has no UNIQUE(title), so we do manual upsert by title.
    with conn.cursor() as cur:
        for item in payload:
            cur.execute(
                """
                SELECT learning_block_id
                FROM public.learning_block
                WHERE title = %s
                """,
                (item["title"],),
            )
            existing = cur.fetchone()

            if existing:
                cur.execute(
                    """
                    UPDATE public.learning_block
                    SET description = %s,
                        block_type = %s,
                        order_index = %s
                    WHERE learning_block_id = %s
                    """,
                    (
                        item["description"],
                        item["block_type"],
                        item["order_index"],
                        existing[0],
                    ),
                )
            else:
                cur.execute(
                    """
                    INSERT INTO public.learning_block
                        (title, description, block_type, order_index)
                    VALUES (%s, %s, %s, %s)
                    """,
                    (
                        item["title"],
                        item["description"],
                        item["block_type"],
                        item["order_index"],
                    ),
                )

        cur.execute("SELECT learning_block_id, title FROM public.learning_block")
        result = {title: block_id for block_id, title in cur.fetchall()}

    print(f"[ok] learning_block: {len(rows)} rows")
    return result


def insert_learning_block_word(
        conn,
        rows: list[dict[str, str]],
        block_ids: dict[str, int],
        word_ids: dict[tuple[str, str], int],
) -> None:
    if not rows:
        return

    payload = []

    for row in rows:
        title = get_required(row, "title", "learning_block_title", "block_title")
        key = (row["writing_form"], row["reading_kana"])

        block_id = block_ids.get(title)
        word_id = word_ids.get(key)

        if not block_id:
            raise KeyError(f"Unknown learning block in learning_block_word.csv: {title}")

        if not word_id:
            raise KeyError(f"Unknown word in learning_block_word.csv: {key}")

        payload.append(
            {
                "learning_block_id": block_id,
                "word_id": word_id,
                "order_index": optional_int(row.get("order_index")) or 0,
            }
        )

    with conn.cursor() as cur:
        execute_batch(
            cur,
            """
            INSERT INTO public.learning_block_word
                (learning_block_id, word_id, order_index)
            VALUES
                (%(learning_block_id)s, %(word_id)s, %(order_index)s)
                ON CONFLICT (learning_block_id, word_id) DO UPDATE SET
                order_index = EXCLUDED.order_index
            """,
            payload,
        )

    print(f"[ok] learning_block_word: {len(payload)} rows")


def insert_learning_block_kanji(
        conn,
        rows: list[dict[str, str]],
        block_ids: dict[str, int],
        kanji_ids: dict[str, int],
) -> None:
    if not rows:
        return

    payload = []

    for row in rows:
        title = get_required(row, "title", "learning_block_title", "block_title")
        kanji = row["kanji"]

        block_id = block_ids.get(title)
        kanji_id = kanji_ids.get(kanji)

        if not block_id:
            raise KeyError(f"Unknown learning block in learning_block_kanji.csv: {title}")

        if not kanji_id:
            raise KeyError(f"Unknown kanji in learning_block_kanji.csv: {kanji}")

        payload.append(
            {
                "learning_block_id": block_id,
                "kanji_id": kanji_id,
                "order_index": optional_int(row.get("order_index")) or 0,
            }
        )

    with conn.cursor() as cur:
        execute_batch(
            cur,
            """
            INSERT INTO public.learning_block_kanji
                (learning_block_id, kanji_id, order_index)
            VALUES
                (%(learning_block_id)s, %(kanji_id)s, %(order_index)s)
                ON CONFLICT (learning_block_id, kanji_id) DO UPDATE SET
                order_index = EXCLUDED.order_index
            """,
            payload,
        )

    print(f"[ok] learning_block_kanji: {len(payload)} rows")


def insert_word_relations(
        conn,
        rows: list[dict[str, str]],
        word_ids: dict[tuple[str, str], int],
) -> None:
    if not rows:
        return

    payload = []

    for row in rows:
        left_key = (row["writing_form"], row["reading_kana"])
        right_key = (row["related_writing_form"], row["related_reading_kana"])

        word_id = word_ids.get(left_key)
        related_word_id = word_ids.get(right_key)

        if not word_id:
            raise KeyError(f"Unknown source word in word_relation.csv: {left_key}")

        if not related_word_id:
            raise KeyError(f"Unknown related word in word_relation.csv: {right_key}")

        payload.append(
            {
                "word_id": word_id,
                "related_word_id": related_word_id,
                "relation_type": row.get("relation_type") or "similar",
                "note": optional_str(row.get("note")),
            }
        )

    with conn.cursor() as cur:
        execute_batch(
            cur,
            """
            INSERT INTO public.word_relation
                (word_id, related_word_id, relation_type, note)
            VALUES
                (%(word_id)s, %(related_word_id)s, %(relation_type)s, %(note)s)
                ON CONFLICT (word_id, related_word_id) DO UPDATE SET
                relation_type = EXCLUDED.relation_type,
                                                              note = EXCLUDED.note
            """,
            payload,
        )

    print(f"[ok] word_relation: {len(payload)} rows")


def connect(config: DbConfig):
    return psycopg2.connect(
        host=config.host,
        port=config.port,
        dbname=config.database,
        user=config.user,
        password=config.password,
    )


def load_seed(csv_dir: Path, env_path: Path) -> None:
    ensure_csv_dir(csv_dir)

    config = load_db_config(env_path)

    print(f"[db] host={config.host} port={config.port} database={config.database} user={config.user}")
    print(f"[csv] dir={csv_dir}")

    conn = connect(config)

    try:
        with conn:
            kanji_ids = insert_kanji(conn, read_csv(csv_dir / "kanji.csv"))
            word_ids = insert_words(conn, read_csv(csv_dir / "word.csv"))

            insert_kanji_readings(conn, read_csv(csv_dir / "kanji_reading.csv"), kanji_ids)
            insert_kanji_meanings(conn, read_csv(csv_dir / "kanji_meaning.csv"), kanji_ids)
            insert_word_meanings(conn, read_csv(csv_dir / "word_meaning.csv"), word_ids)

            insert_kanji_word(
                conn,
                read_csv(csv_dir / "kanji_word.csv"),
                kanji_ids,
                word_ids,
            )

            block_ids = insert_learning_blocks(conn, read_csv(csv_dir / "learning_block.csv"))

            insert_learning_block_word(
                conn,
                read_csv(csv_dir / "learning_block_word.csv"),
                block_ids,
                word_ids,
            )

            insert_learning_block_kanji(
                conn,
                read_csv(csv_dir / "learning_block_kanji.csv"),
                block_ids,
                kanji_ids,
            )

            insert_word_relations(conn, read_csv(csv_dir / "word_relation.csv"), word_ids)

        print("[done] dictionary seed loaded successfully")

    except Exception:
        conn.rollback()
        raise

    finally:
        conn.close()


def parse_args(argv: Iterable[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Load KanjiMap dictionary seed CSV files.")
    parser.add_argument(
        "--env",
        default=".env",
        help="Path to .env file. Default: .env",
    )
    parser.add_argument(
        "--csv-dir",
        default="seed-data",
        help="Directory with CSV seed files. Default: seed-data",
    )
    return parser.parse_args(list(argv))


def main(argv: Iterable[str]) -> int:
    args = parse_args(argv)

    project_root = Path.cwd()
    env_path = (project_root / args.env).resolve()
    csv_dir = (project_root / args.csv_dir).resolve()

    try:
        load_seed(csv_dir=csv_dir, env_path=env_path)
        return 0

    except Exception as error:
        print(f"[error] {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
