package com.eduar.chess.adapter.out.persistence;

import com.eduar.chess.adapter.out.persistence.entity.TrainingCardEntity;
import com.eduar.chess.adapter.out.persistence.mapper.RepertoirePersistenceMapper;
import com.eduar.chess.adapter.out.persistence.mapper.TrainingCardPersistenceMapper;
import com.eduar.chess.adapter.out.persistence.repository.MoveNodeJpaRepository;
import com.eduar.chess.adapter.out.persistence.repository.TrainingCardJpaRepository;
import com.eduar.chess.config.ClockConfig;
import com.eduar.chess.domain.model.CardSource;
import com.eduar.chess.domain.model.MoveNode;
import com.eduar.chess.domain.model.PlayedMove;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.Side;
import com.eduar.chess.domain.model.TrainingCard;
import com.eduar.chess.domain.service.SpacedRepetitionPolicy;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        ClockConfig.class,
        RepertoirePersistenceAdapter.class,
        RepertoirePersistenceMapper.class,
        TrainingCardPersistenceAdapter.class,
        TrainingCardPersistenceMapper.class
})
class PersistenceAdapterIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-11-16T12:00:00Z");

    @Autowired
    private RepertoirePersistenceAdapter repertoireAdapter;

    @Autowired
    private TrainingCardPersistenceAdapter cardAdapter;

    @Autowired
    private MoveNodeJpaRepository moveNodes;

    @Autowired
    private TrainingCardJpaRepository cards;

    @Autowired
    private EntityManager entityManager;

    private static List<PlayedMove> line(String... ucis) {
        List<PlayedMove> moves = new ArrayList<>();
        String fen = "start";
        for (String uci : ucis) {
            String next = fen + " " + uci;
            moves.add(new PlayedMove(uci, uci, fen, next));
            fen = next;
        }
        return moves;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("salva e recarrega um repertório com variantes")
    void savesAndReloadsRepertoire() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"));
        repertoire.addLine(line("e2e4", "e7e5", "g1f3", "g8f6", "d2d3"));
        repertoire.getRoots().get(0).annotate("Abre o centro");

        repertoireAdapter.save(repertoire);
        flushAndClear();
        Repertoire reloaded = repertoireAdapter.findById(repertoire.getId()).orElseThrow();

        assertEquals("Italiana", reloaded.getName());
        assertEquals(Side.WHITE, reloaded.getSide());
        assertEquals(2, reloaded.lines().size());
        assertEquals(repertoire.getRoots().get(0).getId(), reloaded.getRoots().get(0).getId());
        assertEquals("Abre o centro", reloaded.getRoots().get(0).getComment());
    }

    @Test
    @DisplayName("salvar de novo só insere os nós novos e mantém os ids")
    void savingAgainInsertsOnlyNewNodes() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4", "e7e5", "g1f3"));
        repertoireAdapter.save(repertoire);
        flushAndClear();

        Repertoire reloaded = repertoireAdapter.findById(repertoire.getId()).orElseThrow();
        List<MoveNode> created = reloaded.addLine(line("e2e4", "e7e5", "g1f3", "b8c6", "f1c4"));
        repertoireAdapter.save(reloaded);
        flushAndClear();

        assertEquals(1, created.size());
        assertEquals(5, moveNodes.countByRepertoireId(repertoire.getId()));
        Repertoire again = repertoireAdapter.findById(repertoire.getId()).orElseThrow();
        assertEquals(repertoire.getRoots().get(0).getId(), again.getRoots().get(0).getId());
        assertEquals(1, again.lines().size());
        assertEquals(5, again.lines().get(0).nodes().size());
    }

    @Test
    @DisplayName("atualiza o comentário de um nó existente")
    void updatesExistingComment() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        repertoire.addLine(line("e2e4"));
        repertoireAdapter.save(repertoire);
        flushAndClear();

        Repertoire reloaded = repertoireAdapter.findById(repertoire.getId()).orElseThrow();
        reloaded.getRoots().get(0).annotate("Lance mais popular");
        repertoireAdapter.save(reloaded);
        flushAndClear();

        assertEquals("Lance mais popular",
                repertoireAdapter.findById(repertoire.getId()).orElseThrow().getRoots().get(0).getComment());
    }

    @Test
    @DisplayName("repertório inexistente devolve vazio")
    void unknownRepertoireIsEmpty() {
        assertTrue(repertoireAdapter.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("grava os cards com o estado de revisão")
    void savesTrainingCards() {
        Repertoire repertoire = Repertoire.create("Italiana", Side.WHITE);
        List<MoveNode> userMoves = repertoire.addLine(line("e2e4", "e7e5", "g1f3"));
        repertoireAdapter.save(repertoire);
        TrainingCard reviewed = TrainingCard.forRepertoireMove(userMoves.get(0).getId(), NOW);
        reviewed.review(true, NOW, new SpacedRepetitionPolicy());
        TrainingCard fresh = TrainingCard.forRepertoireMove(userMoves.get(1).getId(), NOW);

        cardAdapter.saveAll(List.of(reviewed, fresh));
        flushAndClear();

        TrainingCardEntity stored = cards.findById(reviewed.getId()).orElseThrow();
        assertEquals(CardSource.REPERTOIRE, stored.getSource());
        assertEquals(userMoves.get(0).getId(), stored.getMoveNodeId());
        assertEquals(0, new BigDecimal("2.60").compareTo(stored.getEaseFactor()));
        assertEquals(1, stored.getIntervalDays());
        assertEquals(1, stored.getRepetitions());
        assertEquals(reviewed.getState().dueAt(), stored.getDueAt());
        assertEquals(2, cards.count());
    }
}
