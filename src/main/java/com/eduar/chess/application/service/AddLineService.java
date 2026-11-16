package com.eduar.chess.application.service;

import com.eduar.chess.application.exception.RepertoireNotFoundException;
import com.eduar.chess.application.port.in.AddLineCommand;
import com.eduar.chess.application.port.in.AddLineResult;
import com.eduar.chess.application.port.in.AddLineUseCase;
import com.eduar.chess.application.port.out.RepertoireRepositoryPort;
import com.eduar.chess.application.port.out.TrainingCardRepositoryPort;
import com.eduar.chess.domain.model.MoveNode;
import com.eduar.chess.domain.model.PlayedMove;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.TrainingCard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class AddLineService implements AddLineUseCase {

    private final RepertoireRepositoryPort repertoires;
    private final TrainingCardRepositoryPort trainingCards;
    private final LineConverter lineConverter;
    private final Clock clock;

    public AddLineService(RepertoireRepositoryPort repertoires,
                          TrainingCardRepositoryPort trainingCards,
                          LineConverter lineConverter,
                          Clock clock) {
        this.repertoires = repertoires;
        this.trainingCards = trainingCards;
        this.lineConverter = lineConverter;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AddLineResult addLine(AddLineCommand command) {
        Repertoire repertoire = repertoires.findById(command.repertoireId())
                .orElseThrow(() -> new RepertoireNotFoundException(command.repertoireId()));

        List<PlayedMove> moves = lineConverter.toPlayedMoves(command.sanMoves());
        List<MoveNode> newUserMoves = repertoire.addLine(moves);
        repertoires.save(repertoire);

        Instant now = Instant.now(clock);
        List<TrainingCard> cards = newUserMoves.stream()
                .map(node -> TrainingCard.forRepertoireMove(node.getId(), now))
                .toList();
        if (!cards.isEmpty()) {
            trainingCards.saveAll(cards);
        }
        return new AddLineResult(cards.size());
    }
}
