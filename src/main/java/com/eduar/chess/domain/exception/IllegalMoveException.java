package com.eduar.chess.domain.exception;

public class IllegalMoveException extends DomainException {

    public IllegalMoveException(String san, String fen) {
        super("O lance " + san + " não é válido na posição " + fen);
    }
}
