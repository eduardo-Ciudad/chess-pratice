CREATE TABLE theme (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    slug         VARCHAR(60)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    CONSTRAINT uq_theme_slug UNIQUE (slug)
);

CREATE TABLE study_case (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    title        VARCHAR(150) NOT NULL,
    fen          VARCHAR(100) NOT NULL,
    question     TEXT         NOT NULL,
    explanation  TEXT         NOT NULL,
    opening      VARCHAR(100),
    source_url   VARCHAR(500),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE case_answer (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    study_case_id  UUID        NOT NULL REFERENCES study_case (id) ON DELETE CASCADE,
    uci            VARCHAR(5)  NOT NULL,
    san            VARCHAR(10) NOT NULL,
    is_main        BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_case_answer_move UNIQUE (study_case_id, uci)
);

CREATE UNIQUE INDEX uq_case_answer_main ON case_answer (study_case_id) WHERE is_main;

CREATE TABLE study_case_theme (
    study_case_id  UUID NOT NULL REFERENCES study_case (id) ON DELETE CASCADE,
    theme_id       UUID NOT NULL REFERENCES theme (id) ON DELETE CASCADE,
    PRIMARY KEY (study_case_id, theme_id)
);

CREATE INDEX ix_study_case_theme_theme ON study_case_theme (theme_id);
