package com.eduar.chess.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "move_node")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoveNodeEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "repertoire_id", nullable = false)
    private UUID repertoireId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "ply", nullable = false)
    private short ply;

    @Column(name = "san", nullable = false, length = 10)
    private String san;

    @Column(name = "uci", nullable = false, length = 5)
    private String uci;

    @Column(name = "fen_before", nullable = false, length = 100)
    private String fenBefore;

    @Column(name = "fen_after", nullable = false, length = 100)
    private String fenAfter;

    @Setter
    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Transient
    private boolean newEntity = true;

    public MoveNodeEntity(UUID id, UUID repertoireId, UUID parentId, short ply, String san, String uci,
                          String fenBefore, String fenAfter, String comment) {
        this.id = id;
        this.repertoireId = repertoireId;
        this.parentId = parentId;
        this.ply = ply;
        this.san = san;
        this.uci = uci;
        this.fenBefore = fenBefore;
        this.fenAfter = fenAfter;
        this.comment = comment;
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
