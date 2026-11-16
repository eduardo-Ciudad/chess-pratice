package com.eduar.chess.domain.model;

import com.eduar.chess.domain.exception.ConflictingRepertoireMoveException;
import com.eduar.chess.domain.exception.InvalidLineException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Repertoire {

    private final UUID id;
    private final String name;
    private final Side side;
    private final List<MoveNode> roots = new ArrayList<>();

    private Repertoire(UUID id, String name, Side side) {
        this.id = id;
        this.name = name;
        this.side = side;
    }

    public static Repertoire create(String name, Side side) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do repertório é obrigatório");
        }
        if (side == null) {
            throw new IllegalArgumentException("O lado do repertório é obrigatório");
        }
        return new Repertoire(UUID.randomUUID(), name.trim(), side);
    }

    public List<MoveNode> addLine(List<PlayedMove> moves) {
        if (moves == null || moves.isEmpty()) {
            throw new InvalidLineException("A linha precisa ter pelo menos um lance");
        }
        validateContinuity(moves);
        validateNoConflict(moves);
        return insert(moves);
    }

    public List<Line> lines() {
        List<Line> lines = new ArrayList<>();
        for (MoveNode root : roots) {
            collectLines(root, new ArrayList<>(), lines);
        }
        return lines;
    }

    public Optional<MoveNode> findNode(UUID nodeId) {
        for (MoveNode root : roots) {
            Optional<MoveNode> found = findNode(root, nodeId);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private void validateContinuity(List<PlayedMove> moves) {
        for (int i = 1; i < moves.size(); i++) {
            String previousFen = moves.get(i - 1).fenAfter();
            String currentFen = moves.get(i).fenBefore();
            if (!previousFen.equals(currentFen)) {
                throw new InvalidLineException("O lance " + moves.get(i).san()
                        + " não parte da posição deixada pelo lance anterior");
            }
        }
    }

    private void validateNoConflict(List<PlayedMove> moves) {
        List<MoveNode> siblings = roots;
        int ply = 1;
        for (PlayedMove move : moves) {
            Optional<MoveNode> existing = findByUci(siblings, move.uci());
            if (existing.isEmpty()) {
                if (side.movesOnPly(ply) && !siblings.isEmpty()) {
                    throw new ConflictingRepertoireMoveException(siblings.get(0).getMove().san(), move.san(), ply);
                }
                return;
            }
            siblings = existing.get().getChildren();
            ply++;
        }
    }

    private List<MoveNode> insert(List<PlayedMove> moves) {
        List<MoveNode> createdUserMoves = new ArrayList<>();
        MoveNode parent = null;
        for (PlayedMove move : moves) {
            List<MoveNode> siblings = parent == null ? roots : parent.getChildren();
            Optional<MoveNode> existing = findByUci(siblings, move.uci());
            if (existing.isPresent()) {
                parent = existing.get();
                continue;
            }
            int ply = parent == null ? 1 : parent.getPly() + 1;
            MoveNode node = new MoveNode(UUID.randomUUID(), parent, ply, move);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.addChild(node);
            }
            if (node.isUserMove(side)) {
                createdUserMoves.add(node);
            }
            parent = node;
        }
        return List.copyOf(createdUserMoves);
    }

    private static Optional<MoveNode> findByUci(List<MoveNode> nodes, String uci) {
        return nodes.stream()
                .filter(node -> node.getMove().uci().equals(uci))
                .findFirst();
    }

    private static void collectLines(MoveNode node, List<MoveNode> path, List<Line> lines) {
        path.add(node);
        if (node.isLeaf()) {
            lines.add(new Line(path));
        } else {
            for (MoveNode child : node.getChildren()) {
                collectLines(child, path, lines);
            }
        }
        path.remove(path.size() - 1);
    }

    private static Optional<MoveNode> findNode(MoveNode node, UUID nodeId) {
        if (node.getId().equals(nodeId)) {
            return Optional.of(node);
        }
        for (MoveNode child : node.getChildren()) {
            Optional<MoveNode> found = findNode(child, nodeId);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Side getSide() {
        return side;
    }

    public List<MoveNode> getRoots() {
        return Collections.unmodifiableList(roots);
    }
}
