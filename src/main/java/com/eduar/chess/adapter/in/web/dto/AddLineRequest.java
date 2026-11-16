package com.eduar.chess.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddLineRequest(
        @NotEmpty @Size(max = 200) List<@NotBlank String> moves) {
}
