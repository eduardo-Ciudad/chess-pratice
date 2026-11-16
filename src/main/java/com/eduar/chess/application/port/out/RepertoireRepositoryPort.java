package com.eduar.chess.application.port.out;

import com.eduar.chess.domain.model.Repertoire;

import java.util.Optional;
import java.util.UUID;

public interface RepertoireRepositoryPort {

    Repertoire save(Repertoire repertoire);

    Optional<Repertoire> findById(UUID id);
}
