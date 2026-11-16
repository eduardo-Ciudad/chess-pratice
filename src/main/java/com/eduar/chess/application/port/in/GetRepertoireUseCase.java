package com.eduar.chess.application.port.in;

import java.util.UUID;

public interface GetRepertoireUseCase {

    RepertoireView get(UUID id);
}
