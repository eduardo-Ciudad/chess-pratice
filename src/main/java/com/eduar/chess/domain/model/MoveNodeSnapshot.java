package com.eduar.chess.domain.model;

import java.util.UUID;

public record MoveNodeSnapshot(UUID id, UUID parentId, int ply, PlayedMove move, String comment) {
}
