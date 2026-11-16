package com.eduar.chess.application.service;

import com.eduar.chess.application.fake.FakeChessRules;
import com.eduar.chess.domain.exception.IllegalMoveException;
import com.eduar.chess.domain.model.PlayedMove;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineConverterTest {

    @Test
    @DisplayName("converte os lances encadeando a posição a partir do FEN inicial")
    void chainsPositionsFromInitialFen() {
        FakeChessRules rules = new FakeChessRules();
        LineConverter converter = new LineConverter(rules);

        List<PlayedMove> moves = converter.toPlayedMoves(List.of("e4", "e5", "Nf3"));

        assertEquals(3, moves.size());
        assertEquals(List.of("start", "start e4", "start e4 e5"), rules.receivedFens());
        assertEquals(moves.get(0).fenAfter(), moves.get(1).fenBefore());
        assertEquals(moves.get(1).fenAfter(), moves.get(2).fenBefore());
    }

    @Test
    @DisplayName("lista nula ou vazia vira linha vazia")
    void nullOrEmptyBecomesEmptyLine() {
        LineConverter converter = new LineConverter(new FakeChessRules());

        assertTrue(converter.toPlayedMoves(null).isEmpty());
        assertTrue(converter.toPlayedMoves(List.of()).isEmpty());
    }

    @Test
    @DisplayName("lance ilegal propaga IllegalMoveException")
    void illegalMovePropagates() {
        LineConverter converter = new LineConverter(new FakeChessRules().withIllegalMove("Ke2"));

        assertThrows(IllegalMoveException.class, () -> converter.toPlayedMoves(List.of("e4", "e5", "Ke2")));
    }
}
