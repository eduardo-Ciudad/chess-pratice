package com.eduar.chess.application.service;

import com.eduar.chess.application.exception.InvalidPgnException;
import com.eduar.chess.application.exception.RepertoireNotFoundException;
import com.eduar.chess.application.port.in.ImportPgnCommand;
import com.eduar.chess.application.port.in.ImportPgnResult;
import com.eduar.chess.application.port.in.ImportPgnUseCase;
import com.eduar.chess.application.port.out.ChessRulesPort;
import com.eduar.chess.application.port.out.RepertoireRepositoryPort;
import com.eduar.chess.application.port.out.TrainingCardRepositoryPort;
import com.eduar.chess.domain.model.MoveNode;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.TrainingCard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportPgnService implements ImportPgnUseCase {

    private final RepertoireRepositoryPort repertoires;
    private final TrainingCardRepositoryPort trainingCards;
    private final ChessRulesPort chessRules;
    private final LineConverter lineConverter;
    private final Clock clock;

    public ImportPgnService(RepertoireRepositoryPort repertoires,
                            TrainingCardRepositoryPort trainingCards,
                            ChessRulesPort chessRules,
                            LineConverter lineConverter,
                            Clock clock) {
        this.repertoires = repertoires;
        this.trainingCards = trainingCards;
        this.chessRules = chessRules;
        this.lineConverter = lineConverter;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ImportPgnResult importPgn(ImportPgnCommand command) {
        Repertoire repertoire = repertoires.findById(command.repertoireId())
                .orElseThrow(() -> new RepertoireNotFoundException(command.repertoireId()));

        if (command.pgn() == null || command.pgn().isBlank()) {
            throw new InvalidPgnException("O PGN está vazio");
        }
        List<List<String>> lines = chessRules.parsePgnLines(command.pgn());
        if (lines.isEmpty()) {
            throw new InvalidPgnException("O PGN não tem lances");
        }

        List<MoveNode> newUserMoves = new ArrayList<>();
        for (List<String> line : lines) {
            newUserMoves.addAll(repertoire.addLine(lineConverter.toPlayedMoves(line)));
        }
        repertoires.save(repertoire);

        Instant now = Instant.now(clock);
        List<TrainingCard> cards = newUserMoves.stream()
                .map(node -> TrainingCard.forRepertoireMove(node.getId(), now))
                .toList();
        if (!cards.isEmpty()) {
            trainingCards.saveAll(cards);
        }
        return new ImportPgnResult(lines.size(), cards.size());
    }
}
