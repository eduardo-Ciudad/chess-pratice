package com.eduar.chess.adapter.out.persistence;

import com.eduar.chess.adapter.out.persistence.mapper.TrainingCardPersistenceMapper;
import com.eduar.chess.adapter.out.persistence.repository.TrainingCardJpaRepository;
import com.eduar.chess.application.port.out.TrainingCardRepositoryPort;
import com.eduar.chess.domain.model.TrainingCard;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TrainingCardPersistenceAdapter implements TrainingCardRepositoryPort {

    private final TrainingCardJpaRepository cards;
    private final TrainingCardPersistenceMapper mapper;

    public TrainingCardPersistenceAdapter(TrainingCardJpaRepository cards, TrainingCardPersistenceMapper mapper) {
        this.cards = cards;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void saveAll(List<TrainingCard> newCards) {
        cards.saveAll(newCards.stream().map(mapper::toNewEntity).toList());
    }
}
