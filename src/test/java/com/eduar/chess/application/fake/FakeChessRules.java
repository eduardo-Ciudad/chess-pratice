package com.eduar.chess.application.fake;

import com.eduar.chess.application.port.out.ChessRulesPort;
import com.eduar.chess.domain.exception.IllegalMoveException;
import com.eduar.chess.domain.model.PlayedMove;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FakeChessRules implements ChessRulesPort {

    public static final String INITIAL_FEN = "start";

    private final Set<String> illegalMoves = new HashSet<>();
    private final Map<String, List<List<String>>> pgnLines = new HashMap<>();
    private final List<String> receivedFens = new ArrayList<>();

    public FakeChessRules withIllegalMove(String san) {
        illegalMoves.add(san);
        return this;
    }

    public FakeChessRules withPgn(String pgn, List<List<String>> lines) {
        pgnLines.put(pgn, lines);
        return this;
    }

    @Override
    public String initialFen() {
        return INITIAL_FEN;
    }

    @Override
    public PlayedMove applyMove(String fen, String san) {
        receivedFens.add(fen);
        if (illegalMoves.contains(san)) {
            throw new IllegalMoveException(san, fen);
        }
        return new PlayedMove(san, san, fen, fen + " " + san);
    }

    @Override
    public List<List<String>> parsePgnLines(String pgn) {
        return pgnLines.getOrDefault(pgn, List.of());
    }

    public List<String> receivedFens() {
        return receivedFens;
    }
}
