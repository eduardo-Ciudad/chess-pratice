package com.eduar.chess.domain.model;

import java.util.ArrayList;
import java.util.List;

final class LineFixture {

    static final String START = "start";

    private LineFixture() {
    }

    static List<PlayedMove> line(String... uciMoves) {
        List<PlayedMove> moves = new ArrayList<>();
        String fenBefore = START;
        for (String uci : uciMoves) {
            String fenAfter = fenBefore + " " + uci;
            moves.add(new PlayedMove(uci, uci, fenBefore, fenAfter));
            fenBefore = fenAfter;
        }
        return moves;
    }
}
