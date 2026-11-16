package com.eduar.chess.domain.model;

import java.util.List;

public record Line(List<MoveNode> nodes) {

    public Line {
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("Uma linha precisa ter pelo menos um lance");
        }
        nodes = List.copyOf(nodes);
    }

    public MoveNode leaf() {
        return nodes.get(nodes.size() - 1);
    }
}
