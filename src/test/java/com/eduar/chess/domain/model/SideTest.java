package com.eduar.chess.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SideTest {

    @Test
    @DisplayName("brancas jogam nos ply ímpares")
    void whiteMovesOnOddPlies() {
        assertTrue(Side.WHITE.movesOnPly(1));
        assertFalse(Side.WHITE.movesOnPly(2));
        assertTrue(Side.WHITE.movesOnPly(3));
        assertFalse(Side.WHITE.movesOnPly(4));
    }

    @Test
    @DisplayName("pretas jogam nos ply pares")
    void blackMovesOnEvenPlies() {
        assertFalse(Side.BLACK.movesOnPly(1));
        assertTrue(Side.BLACK.movesOnPly(2));
        assertFalse(Side.BLACK.movesOnPly(3));
        assertTrue(Side.BLACK.movesOnPly(4));
    }

    @Test
    @DisplayName("rejeita ply menor que 1")
    void rejectsPlyLowerThanOne() {
        assertThrows(IllegalArgumentException.class, () -> Side.WHITE.movesOnPly(0));
    }
}
