package com.eduar.chess.domain.model;

public record PlayedMove(String san, String uci, String fenBefore, String fenAfter) {

    public PlayedMove {
        requireText(san, "san");
        requireText(uci, "uci");
        requireText(fenBefore, "fenBefore");
        requireText(fenAfter, "fenAfter");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("O campo " + field + " é obrigatório");
        }
    }
}
