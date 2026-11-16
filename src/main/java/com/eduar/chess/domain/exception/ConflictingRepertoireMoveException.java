package com.eduar.chess.domain.exception;

public class ConflictingRepertoireMoveException extends DomainException {

    public ConflictingRepertoireMoveException(String existingMove, String newMove, int ply) {
        super("O repertório já tem o lance " + existingMove + " nesta posição (ply " + ply
                + "). Não é possível adicionar " + newMove + ".");
    }
}
