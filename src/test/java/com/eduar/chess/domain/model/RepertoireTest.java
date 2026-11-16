package com.eduar.chess.domain.model;

import com.eduar.chess.domain.exception.ConflictingRepertoireMoveException;
import com.eduar.chess.domain.exception.InvalidLineException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.eduar.chess.domain.model.LineFixture.line;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepertoireTest {

    @Test
    @DisplayName("cria repertório com nome e lado")
    void createsRepertoire() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);

        assertEquals("Italiana", repertoire.getName());
        assertEquals(Side.WHITE, repertoire.getSide());
        assertTrue(repertoire.getRoots().isEmpty());
    }

    @Test
    @DisplayName("rejeita repertório sem nome ou sem lado")
    void rejectsInvalidRepertoire() {
        assertThrows(IllegalArgumentException.class, () -> Repertoire.create(" ", Side.WHITE));
        assertThrows(IllegalArgumentException.class, () -> Repertoire.create("Italiana", null));
    }

    @Test
    @DisplayName("cria os nós de uma linha nova com ply incremental")
    void createsNodesWithIncrementalPly() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);

        repertoire.addLine(line("e2e4", "e7e5", "g1f3"));

        assertEquals(1, repertoire.getRoots().size());
        MoveNode first = repertoire.getRoots().get(0);
        MoveNode second = first.getChildren().get(0);
        MoveNode third = second.getChildren().get(0);
        assertEquals("e2e4", first.getMove().uci());
        assertEquals(1, first.getPly());
        assertNull(first.getParent());
        assertEquals("e7e5", second.getMove().uci());
        assertEquals(2, second.getPly());
        assertSame(first, second.getParent());
        assertEquals("g1f3", third.getMove().uci());
        assertEquals(3, third.getPly());
        assertTrue(third.isLeaf());
    }

    @Test
    @DisplayName("reaproveita os nós quando duas linhas têm o começo em comum")
    void reusesNodesForSharedPrefix() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);

        repertoire.addLine(line("e2e4", "e7e5", "g1f3"));
        repertoire.addLine(line("e2e4", "c7c5", "g1f3"));

        assertEquals(1, repertoire.getRoots().size());
        assertEquals(2, repertoire.getRoots().get(0).getChildren().size());
        assertEquals(2, repertoire.lines().size());
    }

    @Test
    @DisplayName("aceita lances diferentes do adversário na mesma posição")
    void acceptsDifferentOpponentMoves() {
        Repertoire repertoire = Repertoire.create("Pretas", Side.BLACK);

        repertoire.addLine(line("e2e4", "e7e5"));
        repertoire.addLine(line("d2d4", "d7d5"));

        assertEquals(2, repertoire.getRoots().size());
    }

    @Test
    @DisplayName("rejeita lance seu diferente na mesma posição em repertório de brancas")
    void rejectsConflictingUserMoveForWhite() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4", "e7e5", "g1f3"));

        assertThrows(ConflictingRepertoireMoveException.class,
                () -> repertoire.addLine(line("e2e4", "e7e5", "f1c4")));
    }

    @Test
    @DisplayName("rejeita primeiro lance diferente em repertório de brancas")
    void rejectsConflictingFirstMoveForWhite() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4"));

        assertThrows(ConflictingRepertoireMoveException.class,
                () -> repertoire.addLine(line("d2d4")));
    }

    @Test
    @DisplayName("rejeita lance seu diferente na mesma posição em repertório de pretas")
    void rejectsConflictingUserMoveForBlack() {
        Repertoire repertoire = Repertoire.create("Pretas", Side.BLACK);
        repertoire.addLine(line("e2e4", "e7e5"));

        assertThrows(ConflictingRepertoireMoveException.class,
                () -> repertoire.addLine(line("e2e4", "c7c5")));
    }

    @Test
    @DisplayName("linha com conflito não altera a árvore")
    void conflictDoesNotChangeTree() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4", "e7e5", "g1f3"));

        assertThrows(ConflictingRepertoireMoveException.class,
                () -> repertoire.addLine(line("e2e4", "e7e5", "f1c4", "g8f6", "d2d3")));

        assertEquals(1, repertoire.lines().size());
        MoveNode blackReply = repertoire.getRoots().get(0).getChildren().get(0);
        assertEquals(1, blackReply.getChildren().size());
        assertEquals("g1f3", blackReply.getChildren().get(0).getMove().uci());
    }

    @Test
    @DisplayName("devolve só os nós novos que são lances seus")
    void returnsOnlyNewUserMoves() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);

        List<MoveNode> firstLine = repertoire.addLine(line("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"));
        List<MoveNode> secondLine = repertoire.addLine(line("e2e4", "e7e5", "g1f3", "g8f6", "d2d3"));

        assertEquals(List.of("e2e4", "g1f3", "f1c4"), ucis(firstLine));
        assertEquals(List.of("d2d3"), ucis(secondLine));
    }

    @Test
    @DisplayName("adicionar a mesma linha duas vezes não duplica nós")
    void addingSameLineTwiceIsIdempotent() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4", "e7e5", "g1f3"));

        List<MoveNode> created = repertoire.addLine(line("e2e4", "e7e5", "g1f3"));

        assertTrue(created.isEmpty());
        assertEquals(1, repertoire.lines().size());
    }

    @Test
    @DisplayName("rejeita linha vazia ou nula")
    void rejectsEmptyLine() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);

        assertThrows(InvalidLineException.class, () -> repertoire.addLine(List.of()));
        assertThrows(InvalidLineException.class, () -> repertoire.addLine(null));
    }

    @Test
    @DisplayName("rejeita linha desconectada")
    void rejectsDisconnectedLine() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        List<PlayedMove> disconnected = List.of(
                new PlayedMove("e4", "e2e4", "start", "pos-a"),
                new PlayedMove("e5", "e7e5", "pos-x", "pos-b"));

        assertThrows(InvalidLineException.class, () -> repertoire.addLine(disconnected));
        assertTrue(repertoire.getRoots().isEmpty());
    }

    @Test
    @DisplayName("lines devolve todos os caminhos da raiz até as folhas")
    void linesReturnsEveryRootToLeafPath() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"));
        repertoire.addLine(line("e2e4", "e7e5", "g1f3", "g8f6", "d2d3"));
        repertoire.addLine(line("e2e4", "c7c6", "d2d4"));

        List<Line> lines = repertoire.lines();

        assertEquals(3, lines.size());
        Set<String> leaves = lines.stream()
                .map(l -> l.leaf().getMove().uci())
                .collect(Collectors.toSet());
        assertEquals(Set.of("f1c4", "d2d3", "d2d4"), leaves);
        Line first = lines.get(0);
        assertEquals(List.of("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"), ucis(first.nodes()));
    }

    @Test
    @DisplayName("encontra nó pelo id")
    void findsNodeById() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        List<MoveNode> created = repertoire.addLine(line("e2e4", "e7e5", "g1f3"));
        MoveNode knight = created.get(1);

        assertSame(knight, repertoire.findNode(knight.getId()).orElseThrow());
        assertTrue(repertoire.findNode(UUID.randomUUID()).isEmpty());
    }

    private static List<String> ucis(List<MoveNode> nodes) {
        return nodes.stream().map(node -> node.getMove().uci()).toList();
    }
}
