package com.eduar.chess.adapter.out.chess;

import com.eduar.chess.application.exception.InvalidPgnException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PgnVariationParser {

    private static final Pattern HEADERS = Pattern.compile("\\[[^\\]]*]");
    private static final Pattern BRACE_COMMENTS = Pattern.compile("\\{[^}]*}");
    private static final Pattern LINE_COMMENTS = Pattern.compile(";[^\\n]*");
    private static final Pattern MOVE_NUMBER_PREFIX = Pattern.compile("^\\d+\\.+");
    private static final Pattern NAG = Pattern.compile("^\\$\\d+$");
    private static final Pattern ANNOTATION_SUFFIX = Pattern.compile("[!?]+$");
    private static final Set<String> RESULTS = Set.of("1-0", "0-1", "1/2-1/2", "*");

    public List<List<String>> parse(String pgn) {
        if (pgn == null || pgn.isBlank()) {
            return List.of();
        }
        Deque<String> tokens = tokenize(pgn);
        List<List<String>> lines = new ArrayList<>();
        parseSequence(tokens, List.of(), lines, 0);
        if (!tokens.isEmpty()) {
            throw new InvalidPgnException("O PGN tem um ')' sem '(' correspondente");
        }
        return lines;
    }

    private void parseSequence(Deque<String> tokens, List<String> prefix, List<List<String>> lines, int depth) {
        List<String> current = new ArrayList<>(prefix);
        boolean closed = false;
        while (!tokens.isEmpty()) {
            String token = tokens.peek();
            if (token.equals(")")) {
                if (depth == 0) {
                    break;
                }
                tokens.pop();
                closed = true;
                break;
            }
            tokens.pop();
            if (token.equals("(")) {
                if (current.isEmpty()) {
                    throw new InvalidPgnException("O PGN abre uma variante antes de qualquer lance");
                }
                parseSequence(tokens, current.subList(0, current.size() - 1), lines, depth + 1);
            } else {
                current.add(token);
            }
        }
        if (depth > 0 && !closed) {
            throw new InvalidPgnException("O PGN tem um '(' sem ')' correspondente");
        }
        if (current.size() > prefix.size()) {
            lines.add(List.copyOf(current));
        }
    }

    private Deque<String> tokenize(String pgn) {
        String text = HEADERS.matcher(pgn).replaceAll(" ");
        text = BRACE_COMMENTS.matcher(text).replaceAll(" ");
        text = LINE_COMMENTS.matcher(text).replaceAll(" ");
        text = text.replace("(", " ( ").replace(")", " ) ");

        Deque<String> tokens = new ArrayDeque<>();
        for (String raw : text.trim().split("\\s+")) {
            String token = MOVE_NUMBER_PREFIX.matcher(raw).replaceFirst("");
            if (token.isEmpty() || RESULTS.contains(token) || NAG.matcher(token).matches()) {
                continue;
            }
            if (!token.equals("(") && !token.equals(")")) {
                token = ANNOTATION_SUFFIX.matcher(token).replaceFirst("");
                if (token.isEmpty()) {
                    continue;
                }
            }
            tokens.add(token);
        }
        return tokens;
    }
}
