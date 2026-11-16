package com.eduar.chess.application.service;

import com.eduar.chess.application.port.in.CreateRepertoireCommand;
import com.eduar.chess.application.port.in.CreateRepertoireUseCase;
import com.eduar.chess.application.port.out.RepertoireRepositoryPort;
import com.eduar.chess.domain.model.Repertoire;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateRepertoireService implements CreateRepertoireUseCase {

    private final RepertoireRepositoryPort repertoires;

    public CreateRepertoireService(RepertoireRepositoryPort repertoires) {
        this.repertoires = repertoires;
    }

    @Override
    @Transactional
    public UUID create(CreateRepertoireCommand command) {
        Repertoire repertoire = Repertoire.create(command.name(), command.side());
        return repertoires.save(repertoire).getId();
    }
}
