package com.eduar.chess.adapter.out.chess;

import com.eduar.chess.application.port.out.ChessRulesPort;
import com.eduar.chess.domain.exception.IllegalMoveException;
import com.eduar.chess.domain.model.PlayedMove;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class ChesslibRulesAdapter implements ChessRulesPort {

    private static final Pattern ANNOTATION_SUFFIX = Pattern.compile("[!?]+$");

    private final PgnVariationParser pgnParser = new PgnVariationParser();

    @Override
    public String initialFen() {
        return new Board().getFen();
    }

    @Override
    public PlayedMove applyMove(String fen, String san) {
        String cleanSan = san == null ? "" : ANNOTATION_SUFFIX.matcher(san.trim()).replaceFirst("");
        if (cleanSan.isEmpty() || cleanSan.contains(" ")) {
            throw new IllegalMoveException(san, fen);
        }
        try {
            MoveList moves = new MoveList(fen);
            moves.loadFromSan(cleanSan);
            if (moves.size() != 1) {
                throw new IllegalMoveException(san, fen);
            }
            Move move = moves.getFirst();
            String normalizedSan = moves.toSanArray()[0];

            Board board = new Board();
            board.loadFromFen(fen);
            if (!board.legalMoves().contains(move)) {
                throw new IllegalMoveException(san, fen);
            }
            board.doMove(move);
            return new PlayedMove(normalizedSan, move.toString().toLowerCase(), fen, board.getFen());
        } catch (IllegalMoveException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalMoveException(san, fen);
        }
    }

    @Override
    public List<List<String>> parsePgnLines(String pgn) {
        return pgnParser.parse(pgn);
    }
}
