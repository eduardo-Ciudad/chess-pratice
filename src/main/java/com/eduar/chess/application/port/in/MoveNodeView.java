package com.eduar.chess.application.port.in;

import java.util.List;
import java.util.UUID;

public record MoveNodeView(
        UUID id,
        String san,
        String uci,
        int ply,
        String fenAfter,
        String comment,
        boolean userMove,
        List<MoveNodeView> children) {
}
