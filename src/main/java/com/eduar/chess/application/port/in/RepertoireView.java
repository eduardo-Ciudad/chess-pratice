package com.eduar.chess.application.port.in;

import com.eduar.chess.domain.model.Side;

import java.util.List;
import java.util.UUID;

public record RepertoireView(UUID id, String name, Side side, List<MoveNodeView> roots) {
}
