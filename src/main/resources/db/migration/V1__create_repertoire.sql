CREATE TABLE repertoire (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(120) NOT NULL,
    side        VARCHAR(5)   NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_repertoire_side CHECK (side IN ('WHITE', 'BLACK'))
);

CREATE TABLE move_node (
    id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    repertoire_id  UUID         NOT NULL REFERENCES repertoire (id) ON DELETE CASCADE,
    parent_id      UUID         REFERENCES move_node (id) ON DELETE CASCADE,
    ply            SMALLINT     NOT NULL,
    san            VARCHAR(10)  NOT NULL,
    uci            VARCHAR(5)   NOT NULL,
    fen_before     VARCHAR(100) NOT NULL,
    fen_after      VARCHAR(100) NOT NULL,
    comment        TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_move_node_ply CHECK (ply >= 1),
    CONSTRAINT uq_move_node_position_move UNIQUE NULLS NOT DISTINCT (repertoire_id, parent_id, uci)
);

CREATE INDEX ix_move_node_repertoire ON move_node (repertoire_id);
CREATE INDEX ix_move_node_parent ON move_node (parent_id);