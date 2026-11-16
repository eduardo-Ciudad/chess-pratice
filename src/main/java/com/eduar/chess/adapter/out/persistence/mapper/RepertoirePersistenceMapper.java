package com.eduar.chess.adapter.out.persistence.mapper;

import com.eduar.chess.adapter.out.persistence.entity.MoveNodeEntity;
import com.eduar.chess.adapter.out.persistence.entity.RepertoireEntity;
import com.eduar.chess.domain.model.MoveNode;
import com.eduar.chess.domain.model.MoveNodeSnapshot;
import com.eduar.chess.domain.model.PlayedMove;
import com.eduar.chess.domain.model.Repertoire;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

@Component
public class RepertoirePersistenceMapper {

    public RepertoireEntity toNewEntity(Repertoire repertoire, Instant now) {
        return new RepertoireEntity(repertoire.getId(), repertoire.getName(), repertoire.getSide(), now);
    }

    public List<MoveNodeEntity> toNodeEntities(Repertoire repertoire) {
        List<MoveNodeEntity> entities = new ArrayList<>();
        Deque<MoveNode> queue = new ArrayDeque<>(repertoire.getRoots());
        while (!queue.isEmpty()) {
            MoveNode node = queue.poll();
            entities.add(toNodeEntity(repertoire.getId(), node));
            queue.addAll(node.getChildren());
        }
        return entities;
    }

    public MoveNodeEntity toNodeEntity(UUID repertoireId, MoveNode node) {
        PlayedMove move = node.getMove();
        UUID parentId = node.getParent() == null ? null : node.getParent().getId();
        return new MoveNodeEntity(node.getId(), repertoireId, parentId, (short) node.getPly(),
                move.san(), move.uci(), move.fenBefore(), move.fenAfter(), node.getComment());
    }

    public Repertoire toDomain(RepertoireEntity entity, List<MoveNodeEntity> nodes) {
        List<MoveNodeSnapshot> snapshots = nodes.stream()
                .map(node -> new MoveNodeSnapshot(
                        node.getId(),
                        node.getParentId(),
                        node.getPly(),
                        new PlayedMove(node.getSan(), node.getUci(), node.getFenBefore(), node.getFenAfter()),
                        node.getComment()))
                .toList();
        return Repertoire.restore(entity.getId(), entity.getName(), entity.getSide(), snapshots);
    }
}
