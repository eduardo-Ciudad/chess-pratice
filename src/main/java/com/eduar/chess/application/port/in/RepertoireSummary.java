package com.eduar.chess.application.port.in;

import com.eduar.chess.domain.model.Side;

import java.util.UUID;

public record RepertoireSummary(UUID id, String name, Side side, int lines) {
}
