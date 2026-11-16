CREATE TABLE training_card (
    id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    source            VARCHAR(20)   NOT NULL,
    move_node_id      UUID          REFERENCES move_node (id) ON DELETE CASCADE,
    study_case_id     UUID          REFERENCES study_case (id) ON DELETE CASCADE,
    ease_factor       NUMERIC(4, 2) NOT NULL DEFAULT 2.50,
    interval_days     INTEGER       NOT NULL DEFAULT 0,
    repetitions       INTEGER       NOT NULL DEFAULT 0,
    due_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    last_reviewed_at  TIMESTAMPTZ,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_training_card_move_node UNIQUE (move_node_id),
    CONSTRAINT uq_training_card_study_case UNIQUE (study_case_id),
    CONSTRAINT ck_training_card_source CHECK (
        (source = 'REPERTOIRE' AND move_node_id IS NOT NULL AND study_case_id IS NULL)
        OR (source = 'CASE' AND study_case_id IS NOT NULL AND move_node_id IS NULL)
    ),
    CONSTRAINT ck_training_card_ease CHECK (ease_factor >= 1.30),
    CONSTRAINT ck_training_card_interval CHECK (interval_days >= 0),
    CONSTRAINT ck_training_card_repetitions CHECK (repetitions >= 0)
);

CREATE INDEX ix_training_card_due ON training_card (due_at);