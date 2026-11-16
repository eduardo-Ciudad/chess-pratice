package com.eduar.chess.application.exception;

import java.util.UUID;

public class RepertoireNotFoundException extends ApplicationException {

    public RepertoireNotFoundException(UUID id) {
        super("Repertório " + id + " não encontrado");
    }
}
