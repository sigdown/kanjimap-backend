#!/usr/bin/env python3
from __future__ import annotations

import argparse
import os
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv
from lxml import etree
import psycopg


ROOT_DIR = Path(__file__).resolve().parent.parent
ENV_PATH = ROOT_DIR / ".env"
load_dotenv(ENV_PATH)


def build_db_url(explicit_db_url: str | None = None) -> str:
    if explicit_db_url:
        return explicit_db_url

    direct_url = os.getenv("DATABASE_URL")
    if direct_url:
        return direct_url

    host = os.getenv("DATABASE_HOST")
    port = os.getenv("DATABASE_PORT", "5432")
    name = os.getenv("DATABASE_NAME")
    user = os.getenv("DATABASE_USER")
    password = os.getenv("DATABASE_PASSWORD")

    if not host or not name or not user or not password:
        raise RuntimeError(
            "Не найдены DATABASE_URL или набор DATABASE_HOST / DATABASE_PORT / DATABASE_NAME / DATABASE_USER / DATABASE_PASSWORD"
        )

    return f"postgresql://{user}:{password}@{host}:{port}/{name}"


def strip_tag(tag: str) -> str:
    return tag.split("}", 1)[1] if "}" in tag else tag


def text_of(elem) -> str | None:
    if elem is None or elem.text is None:
        return None
    value = elem.text.strip()
    return value or None


def child_texts(parent, tag_name: str) -> list[str]:
    result: list[str] = []
    for child in list(parent):
        if strip_tag(child.tag) == tag_name:
            value = text_of(child)
            if value:
                result.append(value)
    return result


def has_child(parent, tag_name: str) -> bool:
    return any(strip_tag(child.tag) == tag_name for child in list(parent))


@dataclass(frozen=True)
class ReadingForm:
    text: str
    restrictions: tuple[str, ...]
    no_kanji: bool


@dataclass(frozen=True)
class MeaningRow:
    meaning: str
    part_of_speech: str | None


def parse_kebs(entry) -> list[str]:
    kebs: list[str] = []

    for child in list(entry):
        if strip_tag(child.tag) != "k_ele":
            continue

        values = child_texts(child, "keb")
        if values:
            kebs.append(values[0])

    return kebs


def parse_readings(entry) -> list[ReadingForm]:
    readings: list[ReadingForm] = []

    for child in list(entry):
        if strip_tag(child.tag) != "r_ele":
            continue

        reb_values = child_texts(child, "reb")
        if not reb_values:
            continue

        restrictions = tuple(child_texts(child, "re_restr"))
        readings.append(
            ReadingForm(
                text=reb_values[0],
                restrictions=restrictions,
                no_kanji=has_child(child, "re_nokanji"),
            )
        )

    return readings


def parse_russian_meanings(entry) -> list[MeaningRow]:
    rows: list[MeaningRow] = []

    for child in list(entry):
        if strip_tag(child.tag) != "sense":
            continue

        pos_values = child_texts(child, "pos")
        part_of_speech = "; ".join(pos_values) if pos_values else None

        for gloss in list(child):
            if strip_tag(gloss.tag) != "gloss":
                continue

            lang = (
                    gloss.attrib.get("{http://www.w3.org/XML/1998/namespace}lang")
                    or gloss.attrib.get("xml:lang")
                    or gloss.attrib.get("lang")
            )

            if lang != "rus":
                continue

            value = text_of(gloss)
            if not value:
                continue

            rows.append(
                MeaningRow(
                    meaning=value,
                    part_of_speech=part_of_speech,
                )
            )

    return list(dict.fromkeys(rows))


def build_word_pairs(kebs: list[str], readings: list[ReadingForm]) -> list[tuple[str, str]]:
    pairs: list[tuple[str, str]] = []

    if not readings:
        return pairs

    if not kebs:
        for reading in readings:
            pairs.append((reading.text, reading.text))
        return list(dict.fromkeys(pairs))

    for reading in readings:
        if reading.no_kanji:
            pairs.append((reading.text, reading.text))
            continue

        if reading.restrictions:
            for keb in kebs:
                if keb in reading.restrictions:
                    pairs.append((keb, reading.text))
        else:
            for keb in kebs:
                pairs.append((keb, reading.text))

    return list(dict.fromkeys(pairs))


def upsert_word(cur, writing_form: str, reading_kana: str) -> int:
    cur.execute(
        """
        INSERT INTO word (writing_form, reading_kana)
        VALUES (%s, %s)
            ON CONFLICT (writing_form, reading_kana) DO NOTHING
        """,
        (writing_form, reading_kana),
    )

    cur.execute(
        """
        SELECT word_id
        FROM word
        WHERE writing_form = %s
          AND reading_kana = %s
        """,
        (writing_form, reading_kana),
    )

    row = cur.fetchone()
    if row is None:
        raise RuntimeError(f"Не найден word_id для {writing_form} / {reading_kana}")

    return row[0]


def replace_meanings(cur, word_id: int, meanings: list[MeaningRow]) -> None:
    cur.execute(
        """
        DELETE FROM word_meaning
        WHERE word_id = %s
        """,
        (word_id,),
    )

    for item in meanings:
        cur.execute(
            """
            INSERT INTO word_meaning (
                word_id,
                meaning,
                example_jp,
                example_translation,
                part_of_speech
            )
            VALUES (%s, %s, NULL, NULL, %s)
            """,
            (word_id, item.meaning, item.part_of_speech),
        )


def import_jmdict_ru(
        xml_path: Path,
        db_url: str,
        limit: int | None,
        commit_every: int,
) -> None:
    imported_entries = 0
    imported_words = 0
    skipped_entries = 0

    context = etree.iterparse(
        str(xml_path),
        events=("end",),
        tag="entry",
        load_dtd=True,
        resolve_entities=True,
        no_network=True,
        huge_tree=True,
        recover=True,
    )

    with psycopg.connect(db_url) as conn:
        with conn.cursor() as cur:
            for _, entry in context:
                kebs = parse_kebs(entry)
                readings = parse_readings(entry)
                meanings = parse_russian_meanings(entry)

                if not readings or not meanings:
                    skipped_entries += 1
                    entry.clear()
                    continue

                pairs = build_word_pairs(kebs, readings)
                if not pairs:
                    skipped_entries += 1
                    entry.clear()
                    continue

                for writing_form, reading_kana in pairs:
                    word_id = upsert_word(cur, writing_form, reading_kana)
                    replace_meanings(cur, word_id, meanings)
                    imported_words += 1

                imported_entries += 1

                if imported_entries % commit_every == 0:
                    conn.commit()
                    print(
                        f"[JMdict-RU] entries={imported_entries}, "
                        f"words={imported_words}, skipped={skipped_entries}"
                    )

                entry.clear()

                parent = entry.getparent()
                if parent is not None:
                    while entry.getprevious() is not None:
                        del parent[0]

                if limit is not None and imported_entries >= limit:
                    break

        conn.commit()

    print(
        f"[DONE] entries={imported_entries}, "
        f"words={imported_words}, skipped={skipped_entries}"
    )


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Импорт русских gloss из JMdict в PostgreSQL"
    )
    parser.add_argument(
        "--file",
        required=True,
        help="Путь к файлу JMdict (можно без расширения, это нормально)",
    )
    parser.add_argument(
        "--db-url",
        default=None,
        help="Явная строка подключения. Если не передана, берётся из .env",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="Ограничить количество импортируемых entry для теста",
    )
    parser.add_argument(
        "--commit-every",
        type=int,
        default=250,
        help="Как часто делать commit",
    )

    args = parser.parse_args()

    xml_path = Path(args.file)
    if not xml_path.exists():
        raise FileNotFoundError(f"Файл не найден: {xml_path}")

    db_url = build_db_url(args.db_url)

    print(f"Using .env: {ENV_PATH}")
    print(f"Import file: {xml_path}")
    print(f"DB URL: {db_url}")

    import_jmdict_ru(
        xml_path=xml_path,
        db_url=db_url,
        limit=args.limit,
        commit_every=args.commit_every,
    )


if __name__ == "__main__":
    main()
