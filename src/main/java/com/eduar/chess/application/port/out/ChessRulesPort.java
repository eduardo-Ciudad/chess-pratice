package com.eduar.chess.application.port.out;

import com.eduar.chess.domain.model.PlayedMove;

import java.util.List;

public interface ChessRulesPort {

    String initialFen();

    PlayedMove applyMove(String fen, String san);

    List<List<String>> parsePgnLines(String pgn);
}
