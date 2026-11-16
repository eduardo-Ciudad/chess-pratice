package com.eduar.chess.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.eduar.chess.domain.model.LineFixture.line;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepertoireRestoreTest {

    private static final UUID REPERTOIRE_ID = UUID.randomUUID();
    private static final UUID E4 = UUID.randomUUID();
    private static final UUID E5 = UUID.randomUUID();
    private static final UUID C5 = UUID.randomUUID();
    private static final UUID NF3 = UUID.randomUUID();

    private static final List<PlayedMove> MAIN = line("e2e4", "e7e5", "g1f3");
    private static final List<PlayedMove> SICILIAN = line("e2e4", "c7c5");

    private static List<MoveNodeSnapshot> snapshots() {
        return List.of(
                new MoveNodeSnapshot(NF3, E5, 3, MAIN.get(2), "Desenvolve e ataca e5"),
                new MoveNodeSnapshot(E4, null, 1, MAIN.get(0), null),
                new MoveNodeSnapshot(C5, E4, 2, SICILIAN.get(1), null),
                new MoveNodeSnapshot(E5, E4, 2, MAIN.get(1), null));
    }

    @Test
    @DisplayName("reconstrói a árvore com os mesmos ids e comentários")
    void restoresTreeWithSameIds() {
        Repertoire repertoire = Repertoire.restore(REPERTOIRE_ID, "Italiana", Side.WHITE, snapshots());

        assertEquals(REPERTOIRE_ID, repertoire.getId());
        assertEquals("Italiana", repertoire.getName());
        assertEquals(Side.WHITE, repertoire.getSide());
        assertEquals(1, repertoire.getRoots().size());
        MoveNode root = repertoire.getRoots().get(0);
        assertEquals(E4, root.getId());
        assertNull(root.getParent());
        assertEquals(2, root.getChildren().size());
        MoveNode knight = repertoire.findNode(NF3).orElseThrow();
        assertEquals(3, knight.getPly());
        assertEquals("Desenvolve e ataca e5", knight.getComment());
        assertSame(repertoire.findNode(E5).orElseThrow(), knight.getParent());
        assertEquals(2, repertoire.lines().size());
    }

    @Test
    @DisplayName("repertório reconstruído reaproveita os nós existentes ao adicionar linha")
    void restoredRepertoireReusesExistingNodes() {
        Repertoire repertoire = Repertoire.restore(REPERTOIRE_ID, "Italiana", Side.WHITE, snapshots());

        List<MoveNode> created = repertoire.addLine(line("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"));

        assertEquals(1, created.size());
        assertEquals("f1c4", created.get(0).getMove().uci());
        assertEquals(NF3, created.get(0).getParent().getParent().getId());
    }

    @Test
    @DisplayName("reconstrói repertório sem nós")
    void restoresEmptyRepertoire() {
        Repertoire repertoire = Repertoire.restore(REPERTOIRE_ID, "Pretas", Side.BLACK, List.of());

        assertTrue(repertoire.getRoots().isEmpty());
    }

    @Test
    @DisplayName("rejeita nó com pai inexistente")
    void rejectsUnknownParent() {
        List<MoveNodeSnapshot> nodes = List.of(
                new MoveNodeSnapshot(E4, null, 1, MAIN.get(0), null),
                new MoveNodeSnapshot(E5, UUID.randomUUID(), 2, MAIN.get(1), null));

        assertThrows(IllegalArgumentException.class,
                () -> Repertoire.restore(REPERTOIRE_ID, "Italiana", Side.WHITE, nodes));
    }

    @Test
    @DisplayName("rejeita ply que não bate com o do pai")
    void rejectsInconsistentPly() {
        List<MoveNodeSnapshot> rootWithWrongPly = List.of(
                new MoveNodeSnapshot(E4, null, 2, MAIN.get(0), null));
        List<MoveNodeSnapshot> childWithWrongPly = List.of(
                new MoveNodeSnapshot(E4, null, 1, MAIN.get(0), null),
                new MoveNodeSnapshot(E5, E4, 3, MAIN.get(1), null));

        assertThrows(IllegalArgumentException.class,
                () -> Repertoire.restore(REPERTOIRE_ID, "Italiana", Side.WHITE, rootWithWrongPly));
        assertThrows(IllegalArgumentException.class,
                () -> Repertoire.restore(REPERTOIRE_ID, "Italiana", Side.WHITE, childWithWrongPly));
    }

    @Test
    @DisplayName("rejeita id de nó duplicado")
    void rejectsDuplicatedNodeId() {
        List<MoveNodeSnapshot> nodes = List.of(
                new MoveNodeSnapshot(E4, null, 1, MAIN.get(0), null),
                new MoveNodeSnapshot(E4, null, 1, MAIN.get(0), null));

        assertThrows(IllegalArgumentException.class,
                () -> Repertoire.restore(REPERTOIRE_ID, "Italiana", Side.WHITE, nodes));
    }

    @Test
    @DisplayName("rejeita reconstrução sem id")
    void rejectsMissingId() {
        assertThrows(IllegalArgumentException.class,
                () -> Repertoire.restore(null, "Italiana", Side.WHITE, List.of()));
    }
}
