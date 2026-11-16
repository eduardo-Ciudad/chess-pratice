package com.eduar.chess.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MoveNode {

    private final UUID id;
    private final MoveNode parent;
    private final int ply;
    private final PlayedMove move;
    private final List<MoveNode> children = new ArrayList<>();
    private String comment;

    MoveNode(UUID id, MoveNode parent, int ply, PlayedMove move) {
        this.id = id;
        this.parent = parent;
        this.ply = ply;
        this.move = move;
    }

    public boolean isUserMove(Side side) {
        return side.movesOnPly(ply);
    }

    public Optional<MoveNode> findChild(String uci) {
        return children.stream()
                .filter(child -> child.move.uci().equals(uci))
                .findFirst();
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void annotate(String comment) {
        this.comment = comment == null || comment.isBlank() ? null : comment.trim();
    }

    void addChild(MoveNode child) {
        children.add(child);
    }

    public UUID getId() {
        return id;
    }

    public MoveNode getParent() {
        return parent;
    }

    public int getPly() {
        return ply;
    }

    public PlayedMove getMove() {
        return move;
    }

    public List<MoveNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public String getComment() {
        return comment;
    }
}
