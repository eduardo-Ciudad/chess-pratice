package com.eduar.chess.application.service;

import com.eduar.chess.application.port.in.ListRepertoiresUseCase;
import com.eduar.chess.application.port.in.RepertoireSummary;
import com.eduar.chess.application.port.out.RepertoireRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ListRepertoiresService implements ListRepertoiresUseCase {

    private final RepertoireRepositoryPort repertoires;

    public ListRepertoiresService(RepertoireRepositoryPort repertoires) {
        this.repertoires = repertoires;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepertoireSummary> list() {
        return repertoires.findAll().stream()
                .map(repertoire -> new RepertoireSummary(
                        repertoire.getId(),
                        repertoire.getName(),
                        repertoire.getSide(),
                        repertoire.lines().size()))
                .sorted(Comparator.comparing(RepertoireSummary::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
