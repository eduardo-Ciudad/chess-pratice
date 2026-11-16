package com.eduar.chess.domain.model;

public enum Side {
    WHITE,
    BLACK;

    public boolean movesOnPly(int ply) {
        if (ply < 1) {
            throw new IllegalArgumentException("O ply deve ser maior ou igual a 1");
        }
        boolean whiteToMove = ply % 2 == 1;
        return this == WHITE ? whiteToMove : !whiteToMove;
    }
}
