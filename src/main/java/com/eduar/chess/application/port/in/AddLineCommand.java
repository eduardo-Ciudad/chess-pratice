package com.eduar.chess.application.port.in;

import java.util.List;
import java.util.UUID;

public record AddLineCommand(UUID repertoireId, List<String> sanMoves) {

    public AddLineCommand {
        if (repertoireId == null) {
            throw new IllegalArgumentException("O id do repertório é obrigatório");
        }
        sanMoves = sanMoves == null ? List.of() : List.copyOf(sanMoves);
    }
}
