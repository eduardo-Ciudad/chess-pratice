package com.eduar.chess.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrainingCardRestoreTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");
    private static final ReviewState STATE = new ReviewState(2.7, 6, 2, NOW, NOW);

    @Test
    @DisplayName("reconstrói o card com id, referência e estado")
    void restoresCard() {
        UUID id = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        TrainingCard card = TrainingCard.restore(id, CardSource.REPERTOIRE, nodeId, null, STATE);

        assertEquals(id, card.getId());
        assertEquals(CardSource.REPERTOIRE, card.getSource());
        assertEquals(nodeId, card.getMoveNodeId());
        assertEquals(STATE, card.getState());
    }

    @Test
    @DisplayName("rejeita referência incoerente com a origem")
    void rejectsInconsistentReference() {
        UUID id = UUID.randomUUID();
        UUID ref = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> TrainingCard.restore(id, CardSource.REPERTOIRE, null, ref, STATE));
        assertThrows(IllegalArgumentException.class,
                () -> TrainingCard.restore(id, CardSource.CASE, ref, null, STATE));
        assertThrows(IllegalArgumentException.class,
                () -> TrainingCard.restore(id, CardSource.REPERTOIRE, ref, ref, STATE));
    }

    @Test
    @DisplayName("rejeita card sem id, origem ou estado")
    void rejectsMissingFields() {
        UUID ref = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> TrainingCard.restore(null, CardSource.REPERTOIRE, ref, null, STATE));
        assertThrows(IllegalArgumentException.class,
                () -> TrainingCard.restore(UUID.randomUUID(), null, ref, null, STATE));
        assertThrows(IllegalArgumentException.class,
                () -> TrainingCard.restore(UUID.randomUUID(), CardSource.REPERTOIRE, ref, null, null));
    }
}
