package com.eduar.chess.adapter.out.persistence.mapper;

import com.eduar.chess.adapter.out.persistence.entity.TrainingCardEntity;
import com.eduar.chess.domain.model.CardSource;
import com.eduar.chess.domain.model.ReviewState;
import com.eduar.chess.domain.model.TrainingCard;
import com.eduar.chess.domain.service.SpacedRepetitionPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingCardPersistenceMapperTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");

    private final TrainingCardPersistenceMapper mapper = new TrainingCardPersistenceMapper();

    @Test
    @DisplayName("converte o card em entidade nova com a facilidade em duas casas")
    void mapsToEntity() {
        TrainingCard card = TrainingCard.forRepertoireMove(UUID.randomUUID(), NOW);
        card.review(true, NOW, new SpacedRepetitionPolicy());

        TrainingCardEntity entity = mapper.toNewEntity(card);

        assertEquals(card.getId(), entity.getId());
        assertEquals(CardSource.REPERTOIRE, entity.getSource());
        assertEquals(card.getMoveNodeId(), entity.getMoveNodeId());
        assertNull(entity.getStudyCaseId());
        assertEquals(new BigDecimal("2.60"), entity.getEaseFactor());
        assertEquals(1, entity.getIntervalDays());
        assertEquals(1, entity.getRepetitions());
        assertEquals(card.getState().dueAt(), entity.getDueAt());
        assertEquals(NOW, entity.getLastReviewedAt());
        assertTrue(entity.isNew());
    }

    @Test
    @DisplayName("ida e volta preserva o card")
    void roundTripPreservesCard() {
        TrainingCard original = TrainingCard.forStudyCase(UUID.randomUUID(), NOW);

        TrainingCard restored = mapper.toDomain(mapper.toNewEntity(original));

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getSource(), restored.getSource());
        assertEquals(original.getStudyCaseId(), restored.getStudyCaseId());
        assertEquals(original.getState(), restored.getState());
    }

    @Test
    @DisplayName("facilidade vinda do banco vira double")
    void easeFactorFromDatabase() {
        ReviewState state = new ReviewState(2.36, 16, 3, NOW, NOW);
        TrainingCard card = TrainingCard.restore(UUID.randomUUID(), CardSource.REPERTOIRE, UUID.randomUUID(), null, state);

        TrainingCard restored = mapper.toDomain(mapper.toNewEntity(card));

        assertEquals(2.36, restored.getState().easeFactor(), 1e-9);
    }
}
