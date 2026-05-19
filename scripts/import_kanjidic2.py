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
            "Не найдены DATABASE_URL или набор "
            "DATABASE_HOST / DATABASE_PORT / DATABASE_NAME / DATABASE_USER / DATABASE_PASSWORD"
        )

    return f"postgresql://{user}:{password}@{host}:{port}/{name}"


def text_of(elem) -> str | None:
    if elem is None or elem.text is None:
        return None
    value = elem.text.strip()
    return value or None


def first_child(parent, tag_name: str):
    for child in list(parent):
        if child.tag == tag_name:
            return child
    return None


def child_texts(parent, tag_name: str) -> list[str]:
    result: list[str] = []
    for child in list(parent):
        if child.tag == tag_name:
            value = text_of(child)
            if value:
                result.append(value)
    return result


@dataclass(frozen=True)
class KanjiReadingRow:
    reading: str
    reading_type: str  # on | kun | nanori


@dataclass(frozen=True)
class KanjiMeaningRow:
    language_code: str
    meaning: str


def normalize_reading_type(r_type: str | None) -> str | None:
    if r_type == "ja_on":
        return "on"
    if r_type == "ja_kun":
        return "kun"
    return None


def parse_character(character):
    literal = text_of(first_child(character, "literal"))
    if not literal:
        return None

    misc = first_child(character, "misc")
    stroke_count: int | None = None
    jlpt_level: str | None = None

    if misc is not None:
        stroke_values = child_texts(misc, "stroke_count")
        if stroke_values:
            try:
                stroke_count = int(stroke_values[0])
            except ValueError:
                stroke_count = None

        jlpt_value = text_of(first_child(misc, "jlpt"))
        if jlpt_value:
            jlpt_level = jlpt_value

    readings: list[KanjiReadingRow] = []
    meanings: list[KanjiMeaningRow] = []

    reading_meaning = first_child(character, "reading_meaning")
    if reading_meaning is not None:
        for child in list(reading_meaning):
            if child.tag == "rmgroup":
                for item in list(child):
                    if item.tag == "reading":
                        reading = text_of(item)
                        reading_type = normalize_reading_type(item.attrib.get("r_type"))
                        if reading and reading_type:
                            readings.append(
                                KanjiReadingRow(
                                    reading=reading,
                                    reading_type=reading_type,
                                )
                            )

                    elif item.tag == "meaning":
                        # Берём meanings без языка или явно английские.
                        m_lang = (
                                item.attrib.get("{http://www.w3.org/XML/1998/namespace}lang")
                                or item.attrib.get("m_lang")
                        )
                        meaning = text_of(item)
                        if meaning and m_lang in (None, "", "en"):
                            meanings.append(
                                KanjiMeaningRow(
                                    language_code="eng",
                                    meaning=meaning,
                                )
                            )

            elif child.tag == "nanori":
                nanori = text_of(child)
                if nanori:
                    readings.append(
                        KanjiReadingRow(
                            reading=nanori,
                            reading_type="nanori",
                        )
                    )

    # убираем дубли, сохраняя порядок
    readings = list(dict.fromkeys(readings))
    meanings = list(dict.fromkeys(meanings))

    return literal, stroke_count, jlpt_level, readings, meanings


def upsert_kanji(cur, symbol: str, stroke_count: int | None, jlpt_level: str | None) -> int:
    cur.execute(
        """
        INSERT INTO kanji (kanji, stroke_count, jlpt_level)
        VALUES (%s, %s, %s)
            ON CONFLICT (kanji) DO UPDATE
                                       SET stroke_count = COALESCE(EXCLUDED.stroke_count, kanji.stroke_count),
                                       jlpt_level = COALESCE(EXCLUDED.jlpt_level, kanji.jlpt_level)
        """,
        (symbol, stroke_count, jlpt_level),
    )

    cur.execute(
        """
        SELECT kanji_id
        FROM kanji
        WHERE kanji = %s
        """,
        (symbol,),
    )
    row = cur.fetchone()
    if row is None:
        raise RuntimeError(f"Не найден kanji_id для символа {symbol}")
    return row[0]


def replace_kanji_children(
        cur,
        kanji_id: int,
        readings: list[KanjiReadingRow],
        meanings: list[KanjiMeaningRow],
) -> None:
    cur.execute(
        "DELETE FROM kanji_reading WHERE kanji_id = %s",
        (kanji_id,),
    )
    cur.execute(
        "DELETE FROM kanji_meaning WHERE kanji_id = %s",
        (kanji_id,),
    )

    for row in readings:
        cur.execute(
            """
            INSERT INTO kanji_reading (
                kanji_id,
                reading,
                reading_type
            )
            VALUES (%s, %s, %s)
                ON CONFLICT DO NOTHING
            """,
            (kanji_id, row.reading, row.reading_type),
        )

    for row in meanings:
        cur.execute(
            """
            INSERT INTO kanji_meaning (
                kanji_id,
                language_code,
                meaning,
                example
            )
            VALUES (%s, %s, %s, NULL)
                ON CONFLICT DO NOTHING
            """,
            (kanji_id, row.language_code, row.meaning),
        )


def import_kanjidic(
        xml_path: Path,
        db_url: str,
        limit: int | None,
        commit_every: int,
) -> None:
    imported = 0
    skipped = 0

    context = etree.iterparse(
        str(xml_path),
        events=("end",),
        tag="character",
        huge_tree=True,
        recover=True,
    )

    with psycopg.connect(db_url) as conn:
        with conn.cursor() as cur:
            for _, character in context:
                parsed = parse_character(character)
                if parsed is None:
                    skipped += 1
                    character.clear()
                    continue

                symbol, stroke_count, jlpt_level, readings, meanings = parsed

                kanji_id = upsert_kanji(
                    cur=cur,
                    symbol=symbol,
                    stroke_count=stroke_count,
                    jlpt_level=jlpt_level,
                )

                replace_kanji_children(
                    cur=cur,
                    kanji_id=kanji_id,
                    readings=readings,
                    meanings=meanings,
                )

                imported += 1

                if imported % commit_every == 0:
                    conn.commit()
                    print(f"[KANJIDIC2] imported={imported}, skipped={skipped}")

                character.clear()
                parent = character.getparent()
                if parent is not None:
                    while character.getprevious() is not None:
                        del parent[0]

                if limit is not None and imported >= limit:
                    break

        conn.commit()

    print(f"[DONE] imported={imported}, skipped={skipped}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Импорт KANJIDIC2 в PostgreSQL")
    parser.add_argument("--file", required=True, help="Путь к файлу kanjidic2.xml")
    parser.add_argument("--db-url", default=None, help="Явная строка подключения")
    parser.add_argument("--limit", type=int, default=None, help="Лимит записей для теста")
    parser.add_argument("--commit-every", type=int, default=500, help="Как часто делать commit")
    args = parser.parse_args()

    xml_path = Path(args.file)
    if not xml_path.exists():
        raise FileNotFoundError(f"Файл не найден: {xml_path}")

    db_url = build_db_url(args.db_url)

    print(f"Using .env: {ENV_PATH}")
    print(f"Import file: {xml_path}")
    print(f"DB URL: {db_url}")

    import_kanjidic(
        xml_path=xml_path,
        db_url=db_url,
        limit=args.limit,
        commit_every=args.commit_every,
    )


if __name__ == "__main__":
    main()
