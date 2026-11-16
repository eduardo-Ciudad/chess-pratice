package com.eduar.chess.application.service;

import com.eduar.chess.application.port.out.ChessRulesPort;
import com.eduar.chess.domain.model.PlayedMove;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LineConverter {

    private final ChessRulesPort chessRules;

    public LineConverter(ChessRulesPort chessRules) {
        this.chessRules = chessRules;
    }

    public List<PlayedMove> toPlayedMoves(List<String> sanMoves) {
        if (sanMoves == null || sanMoves.isEmpty()) {
            return List.of();
        }
        List<PlayedMove> moves = new ArrayList<>();
        String fen = chessRules.initialFen();
        for (String san : sanMoves) {
            PlayedMove move = chessRules.applyMove(fen, san);
            moves.add(move);
            fen = move.fenAfter();
        }
        return moves;
    }
}
