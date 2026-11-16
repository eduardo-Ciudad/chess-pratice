package com.eduar.chess.domain.model;

import com.eduar.chess.domain.service.SpacedRepetitionPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingCardTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");

    private final SpacedRepetitionPolicy policy = new SpacedRepetitionPolicy();

    @Test
    @DisplayName("card de repertório nasce pronto para revisão")
    void repertoireCardIsDueWhenCreated() {
        UUID nodeId = UUID.randomUUID();

        TrainingCard card = TrainingCard.forRepertoireMove(nodeId, NOW);

        assertNotNull(card.getId());
        assertEquals(CardSource.REPERTOIRE, card.getSource());
        assertEquals(nodeId, card.getMoveNodeId());
        assertNull(card.getStudyCaseId());
        assertTrue(card.isDue(NOW));
    }

    @Test
    @DisplayName("card de caso aponta só para o caso")
    void studyCaseCardPointsOnlyToCase() {
        UUID caseId = UUID.randomUUID();

        TrainingCard card = TrainingCard.forStudyCase(caseId, NOW);

        assertEquals(CardSource.CASE, card.getSource());
        assertEquals(caseId, card.getStudyCaseId());
        assertNull(card.getMoveNodeId());
    }

    @Test
    @DisplayName("rejeita card sem referência")
    void rejectsCardWithoutReference() {
        assertThrows(IllegalArgumentException.class, () -> TrainingCard.forRepertoireMove(null, NOW));
        assertThrows(IllegalArgumentException.class, () -> TrainingCard.forStudyCase(null, NOW));
    }

    @Test
    @DisplayName("review atualiza o estado e a data da última revisão")
    void reviewUpdatesState() {
        TrainingCard card = TrainingCard.forRepertoireMove(UUID.randomUUID(), NOW);

        card.review(true, NOW, policy);

        assertEquals(1, card.getState().repetitions());
        assertEquals(NOW, card.getState().lastReviewedAt());
        assertEquals(NOW.plus(Duration.ofDays(1)), card.getState().dueAt());
    }

    @Test
    @DisplayName("isDue é falso antes da data de revisão e verdadeiro a partir dela")
    void isDueRespectsDueDate() {
        TrainingCard card = TrainingCard.forRepertoireMove(UUID.randomUUID(), NOW);
        card.review(true, NOW, policy);

        assertFalse(card.isDue(NOW));
        assertFalse(card.isDue(NOW.plus(Duration.ofHours(23))));
        assertTrue(card.isDue(NOW.plus(Duration.ofDays(1))));
    }
}
