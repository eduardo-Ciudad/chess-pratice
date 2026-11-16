package com.eduar.chess.application.port.in;

import java.util.UUID;

public interface CreateRepertoireUseCase {

    UUID create(CreateRepertoireCommand command);
}
