package com.eduar.chess.application.service;

import com.eduar.chess.application.exception.InvalidPgnException;
import com.eduar.chess.application.exception.RepertoireNotFoundException;
import com.eduar.chess.application.fake.FakeChessRules;
import com.eduar.chess.application.fake.InMemoryRepertoireRepository;
import com.eduar.chess.application.fake.InMemoryTrainingCardRepository;
import com.eduar.chess.application.port.in.ImportPgnCommand;
import com.eduar.chess.application.port.in.ImportPgnResult;
import com.eduar.chess.domain.exception.ConflictingRepertoireMoveException;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.Side;
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

class ImportPgnServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");
    private static final String ITALIAN_PGN = "1. e4 e5 2. Nf3 Nc6 (2... Nf6 3. d3) 3. Bc4";

    private final InMemoryRepertoireRepository repertoires = new InMemoryRepertoireRepository();
    private final InMemoryTrainingCardRepository cards = new InMemoryTrainingCardRepository();
    private final FakeChessRules rules = new FakeChessRules().withPgn(ITALIAN_PGN, List.of(
            List.of("e4", "e5", "Nf3", "Nc6", "Bc4"),
            List.of("e4", "e5", "Nf3", "Nf6", "d3")));
    private final ImportPgnService service = new ImportPgnService(
            repertoires, cards, rules, new LineConverter(rules), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    @DisplayName("adiciona todas as variantes e salva uma única vez")
    void importsEveryVariationAndSavesOnce() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));

        ImportPgnResult result = service.importPgn(new ImportPgnCommand(repertoire.getId(), ITALIAN_PGN));

        assertEquals(2, result.importedLines());
        assertEquals(2, repertoire.lines().size());
        assertEquals(1, repertoires.saveCount());
    }

    @Test
    @DisplayName("cria os cards dos lances novos de todas as variantes")
    void createsCardsForEveryVariation() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));

        ImportPgnResult result = service.importPgn(new ImportPgnCommand(repertoire.getId(), ITALIAN_PGN));

        assertEquals(4, result.createdCards());
        assertEquals(4, cards.cards().size());
    }

    @Test
    @DisplayName("PGN vazio ou sem lances lança InvalidPgnException")
    void emptyPgnIsInvalid() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));

        assertThrows(InvalidPgnException.class,
                () -> service.importPgn(new ImportPgnCommand(repertoire.getId(), "   ")));
        assertThrows(InvalidPgnException.class,
                () -> service.importPgn(new ImportPgnCommand(repertoire.getId(), "[Event \"?\"] *")));

        assertEquals(0, repertoires.saveCount());
    }

    @Test
    @DisplayName("conflito em uma variante não salva nada")
    void conflictInOneVariationSavesNothing() {
        String conflictingPgn = "1. e4 e5 2. Nf3 (2. Bc4)";
        rules.withPgn(conflictingPgn, List.of(
                List.of("e4", "e5", "Nf3"),
                List.of("e4", "e5", "Bc4")));
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));

        assertThrows(ConflictingRepertoireMoveException.class,
                () -> service.importPgn(new ImportPgnCommand(repertoire.getId(), conflictingPgn)));

        assertEquals(0, repertoires.saveCount());
        assertTrue(cards.cards().isEmpty());
    }

    @Test
    @DisplayName("repertório inexistente lança exceção")
    void unknownRepertoire() {
        assertThrows(RepertoireNotFoundException.class,
                () -> service.importPgn(new ImportPgnCommand(UUID.randomUUID(), ITALIAN_PGN)));
    }
}
