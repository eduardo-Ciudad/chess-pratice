package com.eduar.chess.adapter.out.persistence.entity;

import com.eduar.chess.domain.model.CardSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "training_card")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainingCardEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private CardSource source;

    @Column(name = "move_node_id")
    private UUID moveNodeId;

    @Column(name = "study_case_id")
    private UUID studyCaseId;

    @Column(name = "ease_factor", nullable = false, precision = 4, scale = 2)
    private BigDecimal easeFactor;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    @Column(name = "repetitions", nullable = false)
    private int repetitions;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Transient
    private boolean newEntity = true;

    public TrainingCardEntity(UUID id, CardSource source, UUID moveNodeId, UUID studyCaseId,
                              BigDecimal easeFactor, int intervalDays, int repetitions,
                              Instant dueAt, Instant lastReviewedAt) {
        this.id = id;
        this.source = source;
        this.moveNodeId = moveNodeId;
        this.studyCaseId = studyCaseId;
        this.easeFactor = easeFactor;
        this.intervalDays = intervalDays;
        this.repetitions = repetitions;
        this.dueAt = dueAt;
        this.lastReviewedAt = lastReviewedAt;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.newEntity = false;
    }
}
