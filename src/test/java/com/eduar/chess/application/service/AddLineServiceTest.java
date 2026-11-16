package com.eduar.chess.application.service;

import com.eduar.chess.application.exception.RepertoireNotFoundException;
import com.eduar.chess.application.fake.FakeChessRules;
import com.eduar.chess.application.fake.InMemoryRepertoireRepository;
import com.eduar.chess.application.fake.InMemoryTrainingCardRepository;
import com.eduar.chess.application.port.in.AddLineCommand;
import com.eduar.chess.application.port.in.AddLineResult;
import com.eduar.chess.domain.exception.ConflictingRepertoireMoveException;
import com.eduar.chess.domain.exception.IllegalMoveException;
import com.eduar.chess.domain.model.CardSource;
import com.eduar.chess.domain.model.MoveNode;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.Side;
import com.eduar.chess.domain.model.TrainingCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddLineServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");

    private final InMemoryRepertoireRepository repertoires = new InMemoryRepertoireRepository();
    private final InMemoryTrainingCardRepository cards = new InMemoryTrainingCardRepository();
    private final FakeChessRules rules = new FakeChessRules();
    private final AddLineService service = new AddLineService(
            repertoires, cards, new LineConverter(rules), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("salva o repertório com a linha nova")
    void savesRepertoireWithNewLine() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));

        service.addLine(new AddLineCommand(repertoire.getId(), List.of("e4", "e5", "Nf3")));

        assertEquals(1, repertoires.saveCount());
        assertEquals(1, repertoire.lines().size());
        assertEquals(3, repertoire.lines().get(0).nodes().size());
    }

    @Test
    @DisplayName("cria um card para cada lance seu novo e nenhum para o adversário")
    void createsCardsOnlyForUserMoves() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));

        AddLineResult result = service.addLine(
                new AddLineCommand(repertoire.getId(), List.of("e4", "e5", "Nf3", "Nc6", "Bc4")));

        assertEquals(3, result.createdCards());
        assertEquals(3, cards.cards().size());
        List<String> cardMoves = cards.cards().stream()
                .map(card -> repertoire.findNode(card.getMoveNodeId()).orElseThrow())
                .map(MoveNode::getMove)
                .map(move -> move.san())
                .toList();
        assertEquals(List.of("e4", "Nf3", "Bc4"), cardMoves);
        for (TrainingCard card : cards.cards()) {
            assertEquals(CardSource.REPERTOIRE, card.getSource());
            assertTrue(card.isDue(NOW));
        }
    }

    @Test
    @DisplayName("linha que já existe não cria cards")
    void existingLineCreatesNoCards() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));
        service.addLine(new AddLineCommand(repertoire.getId(), List.of("e4", "e5", "Nf3")));

        AddLineResult result = service.addLine(new AddLineCommand(repertoire.getId(), List.of("e4", "e5", "Nf3")));

        assertEquals(0, result.createdCards());
        assertEquals(2, cards.cards().size());
    }

    @Test
    @DisplayName("repertório inexistente lança exceção e não salva nada")
    void unknownRepertoireSavesNothing() {
        UUID unknownId = UUID.randomUUID();

        assertThrows(RepertoireNotFoundException.class,
                () -> service.addLine(new AddLineCommand(unknownId, List.of("e4"))));

        assertEquals(0, repertoires.saveCount());
        assertTrue(cards.cards().isEmpty());
    }

    @Test
    @DisplayName("lance ilegal não salva nada")
    void illegalMoveSavesNothing() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));
        rules.withIllegalMove("Ke2");

        assertThrows(IllegalMoveException.class,
                () -> service.addLine(new AddLineCommand(repertoire.getId(), List.of("e4", "e5", "Ke2"))));

        assertEquals(0, repertoires.saveCount());
        assertTrue(repertoire.getRoots().isEmpty());
        assertTrue(cards.cards().isEmpty());
    }

    @Test
    @DisplayName("conflito no repertório não salva nada")
    void conflictSavesNothing() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));
        service.addLine(new AddLineCommand(repertoire.getId(), List.of("e4", "e5", "Nf3")));

        assertThrows(ConflictingRepertoireMoveException.class,
                () -> service.addLine(new AddLineCommand(repertoire.getId(), List.of("e4", "e5", "Bc4"))));

        assertEquals(1, repertoires.saveCount());
        assertEquals(2, cards.cards().size());
    }

    @Test
    @DisplayName("comando exige o id do repertório")
    void commandRequiresRepertoireId() {
        assertThrows(IllegalArgumentException.class, () -> new AddLineCommand(null, List.of("e4")));
    }
}
