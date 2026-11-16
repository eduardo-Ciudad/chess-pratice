package com.eduar.chess.adapter.out.persistence.mapper;

import com.eduar.chess.adapter.out.persistence.entity.MoveNodeEntity;
import com.eduar.chess.adapter.out.persistence.entity.RepertoireEntity;
import com.eduar.chess.domain.model.MoveNode;
import com.eduar.chess.domain.model.PlayedMove;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.Side;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepertoirePersistenceMapperTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");

    private final RepertoirePersistenceMapper mapper = new RepertoirePersistenceMapper();

    private static List<PlayedMove> line(String... ucis) {
        List<PlayedMove> moves = new ArrayList<>();
        String fen = "start";
        for (String uci : ucis) {
            String next = fen + " " + uci;
            moves.add(new PlayedMove(uci, uci, fen, next));
            fen = next;
        }
        return moves;
    }

    private static Repertoire italian() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"));
        repertoire.addLine(line("e2e4", "e7e5", "g1f3", "g8f6", "d2d3"));
        repertoire.addLine(line("e2e4", "c7c6", "d2d4"));
        repertoire.findNode(repertoire.getRoots().get(0).getId()).orElseThrow().annotate("Abre o centro");
        return repertoire;
    }

    @Test
    @DisplayName("converte o repertório em entidade nova")
    void mapsRepertoireEntity() {
        Repertoire repertoire = italian();

        RepertoireEntity entity = mapper.toNewEntity(repertoire, NOW);

        assertEquals(repertoire.getId(), entity.getId());
        assertEquals("Italiana", entity.getName());
        assertEquals(Side.WHITE, entity.getSide());
        assertEquals(NOW, entity.getUpdatedAt());
        assertTrue(entity.isNew());
    }

    @Test
    @DisplayName("achata a árvore com os pais sempre antes dos filhos")
    void flattensTreeParentsFirst() {
        Repertoire repertoire = italian();

        List<MoveNodeEntity> nodes = mapper.toNodeEntities(repertoire);

        assertEquals(9, nodes.size());
        Set<UUID> seen = new HashSet<>();
        for (MoveNodeEntity node : nodes) {
            assertTrue(node.getParentId() == null || seen.contains(node.getParentId()));
            assertEquals(repertoire.getId(), node.getRepertoireId());
            seen.add(node.getId());
        }
    }

    @Test
    @DisplayName("copia todos os campos do lance para a entidade")
    void mapsNodeFields() {
        Repertoire repertoire = italian();
        MoveNode root = repertoire.getRoots().get(0);

        MoveNodeEntity entity = mapper.toNodeEntity(repertoire.getId(), root);

        assertEquals(root.getId(), entity.getId());
        assertNull(entity.getParentId());
        assertEquals(1, entity.getPly());
        assertEquals("e2e4", entity.getSan());
        assertEquals("e2e4", entity.getUci());
        assertEquals("start", entity.getFenBefore());
        assertEquals("start e2e4", entity.getFenAfter());
        assertEquals("Abre o centro", entity.getComment());
    }

    @Test
    @DisplayName("ida e volta preserva ids, linhas e comentários")
    void roundTripPreservesRepertoire() {
        Repertoire original = italian();
        RepertoireEntity entity = mapper.toNewEntity(original, NOW);
        List<MoveNodeEntity> nodes = mapper.toNodeEntities(original);

        Repertoire restored = mapper.toDomain(entity, nodes);

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getSide(), restored.getSide());
        assertEquals(lineIds(original), lineIds(restored));
        assertEquals("Abre o centro", restored.getRoots().get(0).getComment());
    }

    private static List<List<UUID>> lineIds(Repertoire repertoire) {
        return repertoire.lines().stream()
                .map(line -> line.nodes().stream().map(MoveNode::getId).toList())
                .toList();
    }
}
