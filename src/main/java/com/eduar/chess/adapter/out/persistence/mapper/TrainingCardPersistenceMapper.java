package com.eduar.chess.adapter.out.persistence.mapper;

import com.eduar.chess.adapter.out.persistence.entity.TrainingCardEntity;
import com.eduar.chess.domain.model.ReviewState;
import com.eduar.chess.domain.model.TrainingCard;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TrainingCardPersistenceMapper {

    public TrainingCardEntity toNewEntity(TrainingCard card) {
        ReviewState state = card.getState();
        return new TrainingCardEntity(
                card.getId(),
                card.getSource(),
                card.getMoveNodeId(),
                card.getStudyCaseId(),
                BigDecimal.valueOf(state.easeFactor()).setScale(2, RoundingMode.HALF_UP),
                state.intervalDays(),
                state.repetitions(),
                state.dueAt(),
                state.lastReviewedAt());
    }

    public TrainingCard toDomain(TrainingCardEntity entity) {
        ReviewState state = new ReviewState(
                entity.getEaseFactor().doubleValue(),
                entity.getIntervalDays(),
                entity.getRepetitions(),
                entity.getDueAt(),
                entity.getLastReviewedAt());
        return TrainingCard.restore(entity.getId(), entity.getSource(), entity.getMoveNodeId(),
                entity.getStudyCaseId(), state);
    }
}
