package com.eduar.chess.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImportPgnRequest(
        @NotBlank @Size(max = 100_000) String pgn) {
}
