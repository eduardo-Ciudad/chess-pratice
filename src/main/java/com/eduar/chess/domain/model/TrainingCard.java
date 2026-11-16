package com.eduar.chess.domain.model;

import com.eduar.chess.domain.service.SpacedRepetitionPolicy;

import java.time.Instant;
import java.util.UUID;

public class TrainingCard {

    private final UUID id;
    private final CardSource source;
    private final UUID moveNodeId;
    private final UUID studyCaseId;
    private ReviewState state;

    private TrainingCard(UUID id, CardSource source, UUID moveNodeId, UUID studyCaseId, ReviewState state) {
        this.id = id;
        this.source = source;
        this.moveNodeId = moveNodeId;
        this.studyCaseId = studyCaseId;
        this.state = state;
    }

    public static TrainingCard forRepertoireMove(UUID moveNodeId, Instant now) {
        requireReference(moveNodeId);
        return new TrainingCard(UUID.randomUUID(), CardSource.REPERTOIRE, moveNodeId, null, ReviewState.initial(now));
    }

    public static TrainingCard forStudyCase(UUID studyCaseId, Instant now) {
        requireReference(studyCaseId);
        return new TrainingCard(UUID.randomUUID(), CardSource.CASE, null, studyCaseId, ReviewState.initial(now));
    }

    public static TrainingCard restore(UUID id, CardSource source, UUID moveNodeId, UUID studyCaseId,
                                       ReviewState state) {
        if (id == null || source == null || state == null) {
            throw new IllegalArgumentException("Id, origem e estado do card são obrigatórios");
        }
        boolean consistent = switch (source) {
            case REPERTOIRE -> moveNodeId != null && studyCaseId == null;
            case CASE -> studyCaseId != null && moveNodeId == null;
        };
        if (!consistent) {
            throw new IllegalArgumentException("A referência do card não corresponde à origem " + source);
        }
        return new TrainingCard(id, source, moveNodeId, studyCaseId, state);
    }

    public void review(boolean correct, Instant now, SpacedRepetitionPolicy policy) {
        this.state = policy.next(state, correct, now);
    }

    public boolean isDue(Instant now) {
        return !state.dueAt().isAfter(now);
    }

    private static void requireReference(UUID reference) {
        if (reference == null) {
            throw new IllegalArgumentException("O card precisa apontar para um lance ou para um caso");
        }
    }

    public UUID getId() {
        return id;
    }

    public CardSource getSource() {
        return source;
    }

    public UUID getMoveNodeId() {
        return moveNodeId;
    }

    public UUID getStudyCaseId() {
        return studyCaseId;
    }

    public ReviewState getState() {
        return state;
    }
}
