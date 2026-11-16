package com.eduar.chess.application.fake;

import com.eduar.chess.application.port.out.TrainingCardRepositoryPort;
import com.eduar.chess.domain.model.TrainingCard;

import java.util.ArrayList;
import java.util.List;

public class InMemoryTrainingCardRepository implements TrainingCardRepositoryPort {

    private final List<TrainingCard> cards = new ArrayList<>();

    @Override
    public void saveAll(List<TrainingCard> newCards) {
        cards.addAll(newCards);
    }

    public List<TrainingCard> cards() {
        return cards;
    }
}
