package com.eduar.chess.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.eduar.chess.domain.model.LineFixture.line;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveNodeTest {

    @Test
    @DisplayName("identifica se o lance é seu de acordo com o lado do repertório")
    void identifiesUserMove() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4", "e7e5"));
        MoveNode whiteMove = repertoire.getRoots().get(0);
        MoveNode blackMove = whiteMove.getChildren().get(0);

        assertTrue(whiteMove.isUserMove(Side.WHITE));
        assertFalse(whiteMove.isUserMove(Side.BLACK));
        assertTrue(blackMove.isUserMove(Side.BLACK));
        assertFalse(blackMove.isUserMove(Side.WHITE));
    }

    @Test
    @DisplayName("encontra filho pelo lance em UCI")
    void findsChildByUci() {
        Repertoire repertoire = Repertoire.create("Pretas", Side.BLACK);
        repertoire.addLine(line("e2e4", "e7e5"));
        MoveNode root = repertoire.getRoots().get(0);

        assertEquals("e7e5", root.findChild("e7e5").orElseThrow().getMove().uci());
        assertTrue(root.findChild("c7c5").isEmpty());
    }

    @Test
    @DisplayName("comentário é aparado e vazio vira nulo")
    void annotatesComment() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4"));
        MoveNode node = repertoire.getRoots().get(0);

        node.annotate("  Controla o centro  ");
        assertEquals("Controla o centro", node.getComment());

        node.annotate("   ");
        assertNull(node.getComment());
    }
}
