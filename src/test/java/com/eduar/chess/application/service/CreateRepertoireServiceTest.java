package com.eduar.chess.application.service;

import com.eduar.chess.application.fake.InMemoryRepertoireRepository;
import com.eduar.chess.application.port.in.CreateRepertoireCommand;
import com.eduar.chess.domain.model.Repertoire;
import com.eduar.chess.domain.model.Side;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateRepertoireServiceTest {

    private final InMemoryRepertoireRepository repertoires = new InMemoryRepertoireRepository();
    private final CreateRepertoireService service = new CreateRepertoireService(repertoires);

    @Test
    @DisplayName("salva o repertório e devolve o id")
    void savesAndReturnsId() {
        UUID id = service.create(new CreateRepertoireCommand("Italiana", Side.WHITE));

        Repertoire saved = repertoires.findById(id).orElseThrow();
        assertEquals("Italiana", saved.getName());
        assertEquals(Side.WHITE, saved.getSide());
        assertEquals(1, repertoires.saveCount());
    }

    @Test
    @DisplayName("nome inválido lança exceção e não salva")
    void invalidNameDoesNotSave() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(new CreateRepertoireCommand("  ", Side.WHITE)));

        assertEquals(0, repertoires.saveCount());
    }
}
