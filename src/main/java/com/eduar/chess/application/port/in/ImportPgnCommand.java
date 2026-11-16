package com.eduar.chess.application.port.in;

import java.util.UUID;

public record ImportPgnCommand(UUID repertoireId, String pgn) {

    public ImportPgnCommand {
        if (repertoireId == null) {
            throw new IllegalArgumentException("O id do repertório é obrigatório");
        }
    }
}
