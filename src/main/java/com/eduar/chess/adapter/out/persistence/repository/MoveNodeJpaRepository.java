package com.eduar.chess.adapter.out.persistence.repository;

import com.eduar.chess.adapter.out.persistence.entity.MoveNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MoveNodeJpaRepository extends JpaRepository<MoveNodeEntity, UUID> {

    List<MoveNodeEntity> findByRepertoireIdOrderByPlyAsc(UUID repertoireId);

    List<MoveNodeEntity> findByRepertoireIdInOrderByPlyAsc(Collection<UUID> repertoireIds);

    long countByRepertoireId(UUID repertoireId);
}
