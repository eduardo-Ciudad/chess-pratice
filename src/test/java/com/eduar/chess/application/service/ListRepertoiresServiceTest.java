package com.eduar.chess.application.service;

import com.eduar.chess.application.fake.FakeChessRules;
import com.eduar.chess.application.fake.InMemoryRepertoireRepository;
import com.eduar.chess.application.port.in.RepertoireSummary;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.Side;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListRepertoiresServiceTest {

    private final InMemoryRepertoireRepository repertoires = new InMemoryRepertoireRepository();
    private final LineConverter converter = new LineConverter(new FakeChessRules());
    private final ListRepertoiresService service = new ListRepertoiresService(repertoires);

    @Test
    @DisplayName("lista os repertórios ordenados por nome com a quantidade de linhas")
    void listsRepertoiresWithLineCount() {
        Repertoire italian = repertoires.given(Repertoire.create("Italiana", Side.WHITE));
        italian.addLine(converter.toPlayedMoves(List.of("e4", "e5", "Nf3")));
        italian.addLine(converter.toPlayedMoves(List.of("e4", "c5", "Nf3")));
        repertoires.given(Repertoire.create("Defesa com e5", Side.BLACK));

        List<RepertoireSummary> summaries = service.list();

        assertEquals(2, summaries.size());
        assertEquals("Defesa com e5", summaries.get(0).name());
        assertEquals(Side.BLACK, summaries.get(0).side());
        assertEquals(0, summaries.get(0).lines());
        assertEquals("Italiana", summaries.get(1).name());
        assertEquals(italian.getId(), summaries.get(1).id());
        assertEquals(2, summaries.get(1).lines());
    }

    @Test
    @DisplayName("sem repertórios devolve lista vazia")
    void emptyList() {
        assertTrue(service.list().isEmpty());
    }
}
