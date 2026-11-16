package com.eduar.chess.application.fake;

import com.eduar.chess.application.port.out.RepertoireRepositoryPort;
import com.eduar.chess.domain.model.Repertoire;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryRepertoireRepository implements RepertoireRepositoryPort {

    private final Map<UUID, Repertoire> storage = new HashMap<>();
    private int saveCount;

    public Repertoire given(Repertoire repertoire) {
        storage.put(repertoire.getId(), repertoire);
        return repertoire;
    }

    @Override
    public Repertoire save(Repertoire repertoire) {
        saveCount++;
        storage.put(repertoire.getId(), repertoire);
        return repertoire;
    }

    @Override
    public Optional<Repertoire> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    public int saveCount() {
        return saveCount;
    }
}
