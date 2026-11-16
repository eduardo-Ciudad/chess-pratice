package com.eduar.chess.adapter.out.chess;

import com.eduar.chess.domain.exception.IllegalMoveException;
import com.eduar.chess.domain.model.PlayedMove;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChesslibRulesAdapterTest {

    private static final String START = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final ChesslibRulesAdapter adapter = new ChesslibRulesAdapter();

    @Test
    @DisplayName("FEN inicial é a posição padrão")
    void initialFenIsStandardPosition() {
        assertEquals(START, adapter.initialFen());
    }

    @Test
    @DisplayName("e4 gera e2e4 e a posição seguinte")
    void appliesPawnMove() {
        PlayedMove move = adapter.applyMove(START, "e4");

        assertEquals("e4", move.san());
        assertEquals("e2e4", move.uci());
        assertEquals(START, move.fenBefore());
        assertEquals("rnbqkbnr/pppppppp/8/8/4P3", move.fenAfter().substring(0, 25));
        assertEquals("b", move.fenAfter().split(" ")[1]);
    }

    @Test
    @DisplayName("normaliza o SAN removendo espaços e anotações")
    void normalizesSan() {
        PlayedMove move = adapter.applyMove(START, " Nf3!? ");

        assertEquals("Nf3", move.san());
        assertEquals("g1f3", move.uci());
    }

    @Test
    @DisplayName("roque pequeno vira e1g1")
    void appliesCastling() {
        String fen = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/3P1N2/PPP2PPP/RNBQK2R w KQkq - 1 5";

        PlayedMove move = adapter.applyMove(fen, "O-O");

        assertEquals("O-O", move.san());
        assertEquals("e1g1", move.uci());
    }

    @Test
    @DisplayName("promoção gera UCI com a peça promovida em minúscula")
    void appliesPromotion() {
        String fen = "8/4P3/8/8/8/8/k7/4K3 w - - 0 1";

        PlayedMove move = adapter.applyMove(fen, "e8=Q");

        assertEquals("e7e8q", move.uci());
        assertEquals("e8=Q", move.san());
    }

    @Test
    @DisplayName("lance ilegal lança IllegalMoveException")
    void rejectsIllegalMove() {
        assertThrows(IllegalMoveException.class, () -> adapter.applyMove(START, "e5"));
        assertThrows(IllegalMoveException.class, () -> adapter.applyMove(START, "Ke2"));
        assertThrows(IllegalMoveException.class, () -> adapter.applyMove(START, "xyz"));
        assertThrows(IllegalMoveException.class, () -> adapter.applyMove(START, " "));
    }

    @Test
    @DisplayName("rejeita mais de um lance de uma vez")
    void rejectsMultipleMoves() {
        assertThrows(IllegalMoveException.class, () -> adapter.applyMove(START, "e4 e5"));
    }

    @Test
    @DisplayName("encadeia lances usando o FEN devolvido")
    void chainsMoves() {
        PlayedMove e4 = adapter.applyMove(START, "e4");
        PlayedMove e5 = adapter.applyMove(e4.fenAfter(), "e5");
        PlayedMove nf3 = adapter.applyMove(e5.fenAfter(), "Nf3");

        assertEquals("g1f3", nf3.uci());
        assertEquals(e5.fenAfter(), nf3.fenBefore());
    }

    @Test
    @DisplayName("parsePgnLines delega para o parser de variantes")
    void parsesPgnLines() {
        List<List<String>> lines = adapter.parsePgnLines("1. e4 e5 2. Nf3");

        assertEquals(List.of(List.of("e4", "e5", "Nf3")), lines);
    }
}
