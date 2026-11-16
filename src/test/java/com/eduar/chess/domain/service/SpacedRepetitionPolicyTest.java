package com.eduar.chess.domain.service;

import com.eduar.chess.domain.model.ReviewState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpacedRepetitionPolicyTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");

    private final SpacedRepetitionPolicy policy = new SpacedRepetitionPolicy();

    @Test
    @DisplayName("primeiro acerto agenda a revisão para 1 dia")
    void firstCorrectSchedulesOneDay() {
        ReviewState next = policy.next(ReviewState.initial(NOW), true, NOW);

        assertEquals(1, next.repetitions());
        assertEquals(1, next.intervalDays());
        assertEquals(NOW.plus(Duration.ofDays(1)), next.dueAt());
        assertEquals(NOW, next.lastReviewedAt());
    }

    @Test
    @DisplayName("segundo acerto agenda a revisão para 6 dias")
    void secondCorrectSchedulesSixDays() {
        ReviewState first = policy.next(ReviewState.initial(NOW), true, NOW);

        ReviewState second = policy.next(first, true, NOW);

        assertEquals(2, second.repetitions());
        assertEquals(6, second.intervalDays());
        assertEquals(NOW.plus(Duration.ofDays(6)), second.dueAt());
    }

    @Test
    @DisplayName("a partir do terceiro acerto o intervalo é multiplicado pela facilidade")
    void thirdCorrectMultipliesByEase() {
        ReviewState first = policy.next(ReviewState.initial(NOW), true, NOW);
        ReviewState second = policy.next(first, true, NOW);

        ReviewState third = policy.next(second, true, NOW);

        assertEquals(3, third.repetitions());
        assertEquals(16, third.intervalDays());
    }

    @Test
    @DisplayName("acerto aumenta a facilidade em 0.10")
    void correctIncreasesEase() {
        ReviewState next = policy.next(ReviewState.initial(NOW), true, NOW);

        assertEquals(2.6, next.easeFactor(), 1e-9);
    }

    @Test
    @DisplayName("erro zera repetições e intervalo, revisa na hora e reduz a facilidade em 0.20")
    void wrongResetsProgress() {
        ReviewState current = new ReviewState(2.6, 6, 2, NOW, NOW);

        ReviewState next = policy.next(current, false, NOW);

        assertEquals(0, next.repetitions());
        assertEquals(0, next.intervalDays());
        assertEquals(NOW, next.dueAt());
        assertEquals(NOW, next.lastReviewedAt());
        assertEquals(2.4, next.easeFactor(), 1e-9);
    }

    @Test
    @DisplayName("facilidade nunca fica abaixo de 1.30")
    void easeNeverGoesBelowMinimum() {
        ReviewState nearMinimum = new ReviewState(1.4, 1, 1, NOW, NOW);
        ReviewState atMinimum = new ReviewState(1.3, 1, 1, NOW, NOW);

        assertEquals(1.3, policy.next(nearMinimum, false, NOW).easeFactor(), 1e-9);
        assertEquals(1.3, policy.next(atMinimum, false, NOW).easeFactor(), 1e-9);
    }

    @Test
    @DisplayName("acerto depois de erro recomeça com 1 dia")
    void correctAfterWrongRestartsAtOneDay() {
        ReviewState wrong = policy.next(new ReviewState(2.6, 6, 2, NOW, NOW), false, NOW);

        ReviewState next = policy.next(wrong, true, NOW);

        assertEquals(1, next.repetitions());
        assertEquals(1, next.intervalDays());
    }
}
