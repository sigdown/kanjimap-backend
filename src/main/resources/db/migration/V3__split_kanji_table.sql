BEGIN;

DROP TABLE IF EXISTS kanji_meaning CASCADE;

CREATE TABLE kanji_reading
(
    kanji_reading_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kanji_id         BIGINT       NOT NULL
        REFERENCES kanji (kanji_id)
            ON DELETE CASCADE,
    reading          VARCHAR(100) NOT NULL,
    reading_type     VARCHAR(20)  NOT NULL,
    CONSTRAINT chk_kanji_reading_type
        CHECK (reading_type IN ('on', 'kun', 'nanori')),
    CONSTRAINT uq_kanji_reading
        UNIQUE (kanji_id, reading, reading_type)
);

CREATE TABLE kanji_meaning
(
    kanji_meaning_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kanji_id         BIGINT      NOT NULL
        REFERENCES kanji (kanji_id)
            ON DELETE CASCADE,
    language_code    VARCHAR(10) NOT NULL DEFAULT 'eng',
    meaning          TEXT        NOT NULL,
    example          TEXT,
    CONSTRAINT uq_kanji_meaning
        UNIQUE (kanji_id, language_code, meaning)
);

CREATE INDEX idx_kanji_reading_kanji_id
    ON kanji_reading (kanji_id);

CREATE INDEX idx_kanji_reading_type
    ON kanji_reading (reading_type);

CREATE INDEX idx_kanji_meaning_kanji_id
    ON kanji_meaning (kanji_id);

CREATE INDEX idx_kanji_meaning_language_code
    ON kanji_meaning (language_code);

COMMIT;