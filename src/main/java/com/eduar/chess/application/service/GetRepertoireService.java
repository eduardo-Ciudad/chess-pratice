package com.eduar.chess.application.service;

import com.eduar.chess.application.exception.RepertoireNotFoundException;
import com.eduar.chess.application.port.in.GetRepertoireUseCase;
import com.eduar.chess.application.port.in.MoveNodeView;
import com.eduar.chess.application.port.in.RepertoireView;
import com.eduar.chess.application.port.out.RepertoireRepositoryPort;
import com.eduar.chess.domain.model.MoveNode;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.Side;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GetRepertoireService implements GetRepertoireUseCase {

    private final RepertoireRepositoryPort repertoires;

    public GetRepertoireService(RepertoireRepositoryPort repertoires) {
        this.repertoires = repertoires;
    }

    @Override
    @Transactional(readOnly = true)
    public RepertoireView get(UUID id) {
        Repertoire repertoire = repertoires.findById(id)
                .orElseThrow(() -> new RepertoireNotFoundException(id));
        List<MoveNodeView> roots = repertoire.getRoots().stream()
                .map(node -> toView(node, repertoire.getSide()))
                .toList();
        return new RepertoireView(repertoire.getId(), repertoire.getName(), repertoire.getSide(), roots);
    }

    private MoveNodeView toView(MoveNode node, Side side) {
        List<MoveNodeView> children = node.getChildren().stream()
                .map(child -> toView(child, side))
                .toList();
        return new MoveNodeView(
                node.getId(),
                node.getMove().san(),
                node.getMove().uci(),
                node.getPly(),
                node.getMove().fenAfter(),
                node.getComment(),
                node.isUserMove(side),
                children);
    }
}
