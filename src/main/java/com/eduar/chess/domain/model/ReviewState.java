package com.eduar.chess.domain.model;

import java.time.Instant;

public record ReviewState(double easeFactor, int intervalDays, int repetitions, Instant dueAt, Instant lastReviewedAt) {

    public static final double INITIAL_EASE = 2.5;
    public static final double MIN_EASE = 1.3;

    public ReviewState {
        if (easeFactor < MIN_EASE) {
            throw new IllegalArgumentException("A facilidade não pode ser menor que " + MIN_EASE);
        }
        if (intervalDays < 0) {
            throw new IllegalArgumentException("O intervalo não pode ser negativo");
        }
        if (repetitions < 0) {
            throw new IllegalArgumentException("As repetições não podem ser negativas");
        }
        if (dueAt == null) {
            throw new IllegalArgumentException("A data da próxima revisão é obrigatória");
        }
    }

    public static ReviewState initial(Instant now) {
        return new ReviewState(INITIAL_EASE, 0, 0, now, null);
    }
}
