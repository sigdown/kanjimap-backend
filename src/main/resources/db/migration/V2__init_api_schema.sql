BEGIN;

CREATE TABLE app_user
(
    user_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE word
(
    word_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    writing_form VARCHAR(255) NOT NULL,
    reading_kana VARCHAR(255) NOT NULL,
    jlpt_level   VARCHAR(10),
    topic_name   VARCHAR(100),
    CONSTRAINT uq_word_writing_reading UNIQUE (writing_form, reading_kana)
);

CREATE TABLE word_meaning
(
    meaning_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    word_id             BIGINT NOT NULL REFERENCES word (word_id) ON DELETE CASCADE,
    meaning             TEXT   NOT NULL,
    example_jp          TEXT,
    example_translation TEXT,
    part_of_speech      VARCHAR(50)
);

CREATE TABLE kanji
(
    kanji_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kanji        VARCHAR(16) NOT NULL UNIQUE,
    stroke_count INT,
    jlpt_level   VARCHAR(10)
);

CREATE TABLE kanji_meaning
(
    kanji_meaning_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    kanji_id         BIGINT NOT NULL REFERENCES kanji (kanji_id) ON DELETE CASCADE,
    reading          VARCHAR(100),
    reading_type     VARCHAR(20),
    meaning          TEXT,
    example          TEXT,
    CONSTRAINT chk_kanji_meaning_reading_type
        CHECK (reading_type IS NULL OR reading_type IN ('on', 'kun', 'meaning', 'other'))
);

CREATE TABLE kanji_word
(
    kanji_id       BIGINT NOT NULL REFERENCES kanji (kanji_id) ON DELETE CASCADE,
    word_id        BIGINT NOT NULL REFERENCES word (word_id) ON DELETE CASCADE,
    position_index INT,
    PRIMARY KEY (kanji_id, word_id)
);

CREATE TABLE word_relation
(
    word_id         BIGINT      NOT NULL REFERENCES word (word_id) ON DELETE CASCADE,
    related_word_id BIGINT      NOT NULL REFERENCES word (word_id) ON DELETE CASCADE,
    relation_type   VARCHAR(20) NOT NULL DEFAULT 'similar',
    note            TEXT,
    PRIMARY KEY (word_id, related_word_id),
    CONSTRAINT chk_word_relation_not_self
        CHECK (word_id <> related_word_id),
    CONSTRAINT chk_word_relation_type
        CHECK (relation_type IN ('variant', 'similar', 'confusable'))
);

CREATE TABLE learning_block
(
    learning_block_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    description       TEXT,
    block_type        VARCHAR(20)  NOT NULL DEFAULT 'mixed',
    order_index       INT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_learning_block_type
        CHECK (block_type IN ('word', 'kanji', 'mixed'))
);

CREATE TABLE learning_block_word
(
    learning_block_id BIGINT NOT NULL REFERENCES learning_block (learning_block_id) ON DELETE CASCADE,
    word_id           BIGINT NOT NULL REFERENCES word (word_id) ON DELETE CASCADE,
    order_index       INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (learning_block_id, word_id)
);

CREATE TABLE learning_block_kanji
(
    learning_block_id BIGINT NOT NULL REFERENCES learning_block (learning_block_id) ON DELETE CASCADE,
    kanji_id          BIGINT NOT NULL REFERENCES kanji (kanji_id) ON DELETE CASCADE,
    order_index       INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (learning_block_id, kanji_id)
);

CREATE TABLE user_word_progress
(
    user_id          BIGINT      NOT NULL REFERENCES app_user (user_id) ON DELETE CASCADE,
    word_id          BIGINT      NOT NULL REFERENCES word (word_id) ON DELETE CASCADE,
    status           VARCHAR(20) NOT NULL DEFAULT 'new',
    correct_number   INT         NOT NULL DEFAULT 0,
    wrong_number     INT         NOT NULL DEFAULT 0,
    repetition_level INT         NOT NULL DEFAULT 0,
    last_review_at   TIMESTAMPTZ,
    next_review_at   TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, word_id),
    CONSTRAINT chk_user_word_progress_status
        CHECK (status IN ('new', 'learning', 'review', 'mastered')),
    CONSTRAINT chk_user_word_progress_correct_number
        CHECK (correct_number >= 0),
    CONSTRAINT chk_user_word_progress_wrong_number
        CHECK (wrong_number >= 0),
    CONSTRAINT chk_user_word_progress_repetition_level
        CHECK (repetition_level >= 0)
);

CREATE TABLE user_kanji_progress
(
    user_id          BIGINT      NOT NULL REFERENCES app_user (user_id) ON DELETE CASCADE,
    kanji_id         BIGINT      NOT NULL REFERENCES kanji (kanji_id) ON DELETE CASCADE,
    status           VARCHAR(20) NOT NULL DEFAULT 'new',
    correct_number   INT         NOT NULL DEFAULT 0,
    wrong_number     INT         NOT NULL DEFAULT 0,
    repetition_level INT         NOT NULL DEFAULT 0,
    last_review_at   TIMESTAMPTZ,
    next_review_at   TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, kanji_id),
    CONSTRAINT chk_user_kanji_progress_status
        CHECK (status IN ('new', 'learning', 'review', 'mastered')),
    CONSTRAINT chk_user_kanji_progress_correct_number
        CHECK (correct_number >= 0),
    CONSTRAINT chk_user_kanji_progress_wrong_number
        CHECK (wrong_number >= 0),
    CONSTRAINT chk_user_kanji_progress_repetition_level
        CHECK (repetition_level >= 0)
);

CREATE INDEX idx_word_meaning_word_id
    ON word_meaning (word_id);

CREATE INDEX idx_kanji_meaning_kanji_id
    ON kanji_meaning (kanji_id);

CREATE INDEX idx_kanji_word_word_id
    ON kanji_word (word_id);

CREATE INDEX idx_word_relation_related_word_id
    ON word_relation (related_word_id);

CREATE INDEX idx_word_relation_relation_type
    ON word_relation (relation_type);

CREATE INDEX idx_learning_block_order_index
    ON learning_block (order_index);

CREATE INDEX idx_learning_block_word_word_id
    ON learning_block_word (word_id);

CREATE INDEX idx_learning_block_kanji_kanji_id
    ON learning_block_kanji (kanji_id);

CREATE INDEX idx_user_word_progress_next_review
    ON user_word_progress (user_id, next_review_at);

CREATE INDEX idx_user_kanji_progress_next_review
    ON user_kanji_progress (user_id, next_review_at);

CREATE INDEX idx_word_writing_form
    ON word (writing_form);

CREATE INDEX idx_word_reading_kana
    ON word (reading_kana);

CREATE INDEX idx_kanji_symbol
    ON kanji (kanji);

COMMIT;