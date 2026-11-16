package com.eduar.chess.application.port.in;

import com.eduar.chess.domain.model.Side;

public record CreateRepertoireCommand(String name, Side side) {
}
