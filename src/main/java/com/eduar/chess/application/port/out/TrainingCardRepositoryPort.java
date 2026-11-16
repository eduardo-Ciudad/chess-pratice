package com.eduar.chess.application.port.out;

import com.eduar.chess.domain.model.TrainingCard;

import java.util.List;

public interface TrainingCardRepositoryPort {

    void saveAll(List<TrainingCard> cards);
}
