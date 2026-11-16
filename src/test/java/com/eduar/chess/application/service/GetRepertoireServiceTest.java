package com.eduar.chess.application.service;

import com.eduar.chess.application.exception.RepertoireNotFoundException;
import com.eduar.chess.application.fake.FakeChessRules;
import com.eduar.chess.application.fake.InMemoryRepertoireRepository;
import com.eduar.chess.application.port.in.MoveNodeView;
import com.eduar.chess.application.port.in.RepertoireView;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.Side;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetRepertoireServiceTest {

    private final InMemoryRepertoireRepository repertoires = new InMemoryRepertoireRepository();
    private final LineConverter converter = new LineConverter(new FakeChessRules());
    private final GetRepertoireService service = new GetRepertoireService(repertoires);

    @Test
    @DisplayName("monta a árvore com os dados de cada lance")
    void buildsTree() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));
        repertoire.addLine(converter.toPlayedMoves(List.of("e4", "e5", "Nf3")));
        repertoire.addLine(converter.toPlayedMoves(List.of("e4", "c5", "Nf3")));
        repertoire.getRoots().get(0).annotate("Abre o centro");

        RepertoireView view = service.get(repertoire.getId());

        assertEquals(repertoire.getId(), view.id());
        assertEquals("Italiana", view.name());
        assertEquals(Side.WHITE, view.side());
        assertEquals(1, view.roots().size());
        MoveNodeView e4 = view.roots().get(0);
        assertEquals("e4", e4.san());
        assertEquals(1, e4.ply());
        assertEquals("start e4", e4.fenAfter());
        assertEquals("Abre o centro", e4.comment());
        assertEquals(2, e4.children().size());
        assertEquals("Nf3", e4.children().get(0).children().get(0).san());
    }

    @Test
    @DisplayName("marca os lances seus em repertório de brancas")
    void marksUserMovesForWhite() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Italiana", Side.WHITE));
        repertoire.addLine(converter.toPlayedMoves(List.of("e4", "e5")));

        MoveNodeView e4 = service.get(repertoire.getId()).roots().get(0);

        assertTrue(e4.userMove());
        assertFalse(e4.children().get(0).userMove());
    }

    @Test
    @DisplayName("marca os lances seus em repertório de pretas")
    void marksUserMovesForBlack() {
        Repertoire repertoire = repertoires.given(Repertoire.create("Pretas", Side.BLACK));
        repertoire.addLine(converter.toPlayedMoves(List.of("e4", "e5")));

        MoveNodeView e4 = service.get(repertoire.getId()).roots().get(0);

        assertFalse(e4.userMove());
        assertTrue(e4.children().get(0).userMove());
    }

    @Test
    @DisplayName("repertório inexistente lança RepertoireNotFoundException")
    void unknownRepertoire() {
        assertThrows(RepertoireNotFoundException.class, () -> service.get(UUID.randomUUID()));
    }
}
