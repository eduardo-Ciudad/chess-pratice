package com.eduar.chess.adapter.in.web.dto;

import com.eduar.chess.domain.model.Side;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRepertoireRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull Side side) {
}
