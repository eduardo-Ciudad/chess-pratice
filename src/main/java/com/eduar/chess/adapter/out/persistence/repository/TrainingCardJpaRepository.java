package com.eduar.chess.adapter.out.persistence.repository;

import com.eduar.chess.adapter.out.persistence.entity.TrainingCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrainingCardJpaRepository extends JpaRepository<TrainingCardEntity, UUID> {
}
