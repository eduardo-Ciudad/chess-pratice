package com.eduar.chess.adapter.out.persistence.repository;

import com.eduar.chess.adapter.out.persistence.entity.RepertoireEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepertoireJpaRepository extends JpaRepository<RepertoireEntity, UUID> {
}
