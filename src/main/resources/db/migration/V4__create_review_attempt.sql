CREATE TABLE review_attempt (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    training_card_id  UUID        NOT NULL REFERENCES training_card (id) ON DELETE CASCADE,
    played_uci        VARCHAR(5)  NOT NULL,
    correct           BOOLEAN     NOT NULL,
    attempted_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_review_attempt_card ON review_attempt (training_card_id, attempted_at);