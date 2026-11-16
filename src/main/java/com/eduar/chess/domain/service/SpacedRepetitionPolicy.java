package com.eduar.chess.domain.service;

import com.eduar.chess.domain.model.ReviewState;

import java.time.Duration;
import java.time.Instant;

public class SpacedRepetitionPolicy {

    static final double EASE_BONUS = 0.10;
    static final double EASE_PENALTY = 0.20;
    static final int FIRST_INTERVAL_DAYS = 1;
    static final int SECOND_INTERVAL_DAYS = 6;

    public ReviewState next(ReviewState current, boolean correct, Instant now) {
        return correct ? afterCorrect(current, now) : afterWrong(current, now);
    }

    private ReviewState afterCorrect(ReviewState current, Instant now) {
        int repetitions = current.repetitions() + 1;
        int interval = switch (repetitions) {
            case 1 -> FIRST_INTERVAL_DAYS;
            case 2 -> SECOND_INTERVAL_DAYS;
            default -> (int) Math.round(current.intervalDays() * current.easeFactor());
        };
        double ease = round(current.easeFactor() + EASE_BONUS);
        return new ReviewState(ease, interval, repetitions, now.plus(Duration.ofDays(interval)), now);
    }

    private ReviewState afterWrong(ReviewState current, Instant now) {
        double ease = Math.max(ReviewState.MIN_EASE, round(current.easeFactor() - EASE_PENALTY));
        return new ReviewState(ease, 0, 0, now, now);
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
