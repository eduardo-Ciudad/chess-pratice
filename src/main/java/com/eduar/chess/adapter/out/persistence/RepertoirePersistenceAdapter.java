package com.eduar.chess.adapter.out.persistence;

import com.eduar.chess.adapter.out.persistence.entity.MoveNodeEntity;
import com.eduar.chess.adapter.out.persistence.entity.RepertoireEntity;
import com.eduar.chess.adapter.out.persistence.mapper.RepertoirePersistenceMapper;
import com.eduar.chess.adapter.out.persistence.repository.MoveNodeJpaRepository;
import com.eduar.chess.adapter.out.persistence.repository.RepertoireJpaRepository;
import com.eduar.chess.application.port.out.RepertoireRepositoryPort;
import com.eduar.chess.domain.model.Repertoire;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RepertoirePersistenceAdapter implements RepertoireRepositoryPort {

    private final RepertoireJpaRepository repertoires;
    private final MoveNodeJpaRepository moveNodes;
    private final RepertoirePersistenceMapper mapper;
    private final Clock clock;

    public RepertoirePersistenceAdapter(RepertoireJpaRepository repertoires,
                                        MoveNodeJpaRepository moveNodes,
                                        RepertoirePersistenceMapper mapper,
                                        Clock clock) {
        this.repertoires = repertoires;
        this.moveNodes = moveNodes;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Repertoire save(Repertoire repertoire) {
        Instant now = Instant.now(clock);
        RepertoireEntity entity = repertoires.findById(repertoire.getId())
                .orElseGet(() -> mapper.toNewEntity(repertoire, now));
        entity.setUpdatedAt(now);
        repertoires.save(entity);

        Map<UUID, MoveNodeEntity> existing = moveNodes.findByRepertoireIdOrderByPlyAsc(repertoire.getId()).stream()
                .collect(Collectors.toMap(MoveNodeEntity::getId, Function.identity()));

        List<MoveNodeEntity> newNodes = new ArrayList<>();
        for (MoveNodeEntity node : mapper.toNodeEntities(repertoire)) {
            MoveNodeEntity stored = existing.get(node.getId());
            if (stored == null) {
                newNodes.add(node);
            } else if (!Objects.equals(stored.getComment(), node.getComment())) {
                stored.setComment(node.getComment());
            }
        }
        moveNodes.saveAll(newNodes);
        return repertoire;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Repertoire> findById(UUID id) {
        return repertoires.findById(id)
                .map(entity -> mapper.toDomain(entity, moveNodes.findByRepertoireIdOrderByPlyAsc(id)));
    }
}
