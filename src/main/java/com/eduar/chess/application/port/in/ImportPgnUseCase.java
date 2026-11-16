package com.eduar.chess.application.port.in;

public interface ImportPgnUseCase {

    ImportPgnResult importPgn(ImportPgnCommand command);
}
