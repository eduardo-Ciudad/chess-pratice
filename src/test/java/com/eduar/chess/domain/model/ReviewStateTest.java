package com.eduar.chess.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewStateTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");

    @Test
    @DisplayName("estado inicial começa com facilidade 2.5 e revisão imediata")
    void initialState() {
        ReviewState state = ReviewState.initial(NOW);

        assertEquals(2.5, state.easeFactor(), 1e-9);
        assertEquals(0, state.intervalDays());
        assertEquals(0, state.repetitions());
        assertEquals(NOW, state.dueAt());
        assertNull(state.lastReviewedAt());
    }

    @Test
    @DisplayName("rejeita valores inválidos")
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new ReviewState(1.29, 0, 0, NOW, null));
        assertThrows(IllegalArgumentException.class, () -> new ReviewState(2.5, -1, 0, NOW, null));
        assertThrows(IllegalArgumentException.class, () -> new ReviewState(2.5, 0, -1, NOW, null));
        assertThrows(IllegalArgumentException.class, () -> new ReviewState(2.5, 0, 0, null, null));
    }
}
