package com.eduar.chess.adapter.out.chess;

import com.eduar.chess.application.exception.InvalidPgnException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PgnVariationParserTest {

    private final PgnVariationParser parser = new PgnVariationParser();

    @Test
    @DisplayName("linha simples vira uma única lista de lances")
    void parsesSingleLine() {
        List<List<String>> lines = parser.parse("1. e4 e5 2. Nf3 Nc6 3. Bc4");

        assertEquals(List.of(List.of("e4", "e5", "Nf3", "Nc6", "Bc4")), lines);
    }

    @Test
    @DisplayName("variante substitui o lance anterior e vira uma linha completa")
    void parsesVariation() {
        List<List<String>> lines = parser.parse("1. e4 e5 2. Nf3 Nc6 (2... Nf6 3. d3) 3. Bc4");

        assertEquals(Set.of(
                List.of("e4", "e5", "Nf3", "Nc6", "Bc4"),
                List.of("e4", "e5", "Nf3", "Nf6", "d3")), Set.copyOf(lines));
        assertEquals(2, lines.size());
    }

    @Test
    @DisplayName("variante dentro de variante")
    void parsesNestedVariations() {
        List<List<String>> lines = parser.parse("1. e4 e5 (1... c5 2. Nf3 (2. c3 d5) 2... d6) 2. Nf3");

        assertEquals(Set.of(
                List.of("e4", "e5", "Nf3"),
                List.of("e4", "c5", "Nf3", "d6"),
                List.of("e4", "c5", "c3", "d5")), Set.copyOf(lines));
        assertEquals(3, lines.size());
    }

    @Test
    @DisplayName("ignora cabeçalhos, comentários, NAGs, anotações e resultado")
    void ignoresNonMoveTokens() {
        String pgn = """
                [Event "Estudo"]
                [White "Eduardo"]

                1. e4 {controla o centro} e5 2. Nf3! $1 Nc6?! ; comentário de linha
                3. Bc4 1-0
                """;

        List<List<String>> lines = parser.parse(pgn);

        assertEquals(List.of(List.of("e4", "e5", "Nf3", "Nc6", "Bc4")), lines);
    }

    @Test
    @DisplayName("aceita número colado no lance e lances das pretas com reticências")
    void acceptsCompactNumbering() {
        List<List<String>> lines = parser.parse("1.e4 e5 2.Nf3 2...Nc6 3.O-O-O *");

        assertEquals(List.of(List.of("e4", "e5", "Nf3", "Nc6", "O-O-O")), lines);
    }

    @Test
    @DisplayName("PGN sem lances devolve lista vazia")
    void pgnWithoutMovesReturnsEmpty() {
        assertTrue(parser.parse("[Event \"?\"] *").isEmpty());
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse(null).isEmpty());
    }

    @Test
    @DisplayName("parênteses desbalanceados lançam InvalidPgnException")
    void rejectsUnbalancedParentheses() {
        assertThrows(InvalidPgnException.class, () -> parser.parse("1. e4 e5 (1... c5"));
        assertThrows(InvalidPgnException.class, () -> parser.parse("1. e4 e5) 2. Nf3"));
    }

    @Test
    @DisplayName("variante antes de qualquer lance lança InvalidPgnException")
    void rejectsVariationBeforeAnyMove() {
        assertThrows(InvalidPgnException.class, () -> parser.parse("(1. d4) 1. e4"));
    }
}
