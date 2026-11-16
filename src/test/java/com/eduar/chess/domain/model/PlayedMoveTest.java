package com.eduar.chess.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayedMoveTest {

    @Test
    @DisplayName("aceita lance com todos os campos preenchidos")
    void acceptsCompleteMove() {
        assertDoesNotThrow(() -> new PlayedMove("e4", "e2e4", "fen-a", "fen-b"));
    }

    @Test
    @DisplayName("rejeita lance com campo vazio ou nulo")
    void rejectsBlankFields() {
        assertThrows(IllegalArgumentException.class, () -> new PlayedMove(" ", "e2e4", "fen-a", "fen-b"));
        assertThrows(IllegalArgumentException.class, () -> new PlayedMove("e4", null, "fen-a", "fen-b"));
        assertThrows(IllegalArgumentException.class, () -> new PlayedMove("e4", "e2e4", "", "fen-b"));
        assertThrows(IllegalArgumentException.class, () -> new PlayedMove("e4", "e2e4", "fen-a", null));
    }
}
