package com.eduar.chess.adapter.in.web;

import com.eduar.chess.application.exception.InvalidPgnException;
import com.eduar.chess.application.exception.RepertoireNotFoundException;
import com.eduar.chess.application.port.in.AddLineCommand;
import com.eduar.chess.application.port.in.AddLineResult;
import com.eduar.chess.application.port.in.AddLineUseCase;
import com.eduar.chess.application.port.in.CreateRepertoireCommand;
import com.eduar.chess.application.port.in.CreateRepertoireUseCase;
import com.eduar.chess.application.port.in.GetRepertoireUseCase;
import com.eduar.chess.application.port.in.ImportPgnCommand;
import com.eduar.chess.application.port.in.ImportPgnResult;
import com.eduar.chess.application.port.in.ImportPgnUseCase;
import com.eduar.chess.application.port.in.ListRepertoiresUseCase;
import com.eduar.chess.application.port.in.MoveNodeView;
import com.eduar.chess.application.port.in.RepertoireSummary;
import com.eduar.chess.application.port.in.RepertoireView;
import com.eduar.chess.domain.exception.ConflictingRepertoireMoveException;
import com.eduar.chess.domain.exception.IllegalMoveException;
import com.eduar.chess.domain.model.Side;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepertoireController.class)
class RepertoireControllerTest {

    private static final UUID ID = UUID.fromString("3f1c2a9e-0000-4000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateRepertoireUseCase createRepertoire;

    @MockitoBean
    private ListRepertoiresUseCase listRepertoires;

    @MockitoBean
    private GetRepertoireUseCase getRepertoire;

    @MockitoBean
    private AddLineUseCase addLine;

    @MockitoBean
    private ImportPgnUseCase importPgn;

    private static String json(String body) {
        return body.replace('\'', '"');
    }

    @Test
    @DisplayName("POST cria o repertório e devolve 201 com Location")
    void createsRepertoire() throws Exception {
        when(createRepertoire.create(new CreateRepertoireCommand("Italiana", Side.WHITE))).thenReturn(ID);

        mockMvc.perform(post("/api/repertoires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'name':'Italiana','side':'WHITE'}")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/repertoires/" + ID))
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    @DisplayName("POST com nome vazio devolve 400 com o campo inválido")
    void rejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/repertoires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'name':' ','side':'WHITE'}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.errors.name").exists());

        verify(createRepertoire, never()).create(any());
    }

    @Test
    @DisplayName("POST com lado desconhecido devolve 400")
    void rejectsUnknownSide() throws Exception {
        mockMvc.perform(post("/api/repertoires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'name':'Italiana','side':'PURPLE'}")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET lista os repertórios")
    void listsRepertoires() throws Exception {
        when(listRepertoires.list()).thenReturn(List.of(new RepertoireSummary(ID, "Italiana", Side.WHITE, 3)));

        mockMvc.perform(get("/api/repertoires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ID.toString()))
                .andExpect(jsonPath("$[0].name").value("Italiana"))
                .andExpect(jsonPath("$[0].side").value("WHITE"))
                .andExpect(jsonPath("$[0].lines").value(3));
    }

    @Test
    @DisplayName("GET por id devolve a árvore de lances")
    void getsRepertoireTree() throws Exception {
        MoveNodeView e5 = new MoveNodeView(UUID.randomUUID(), "e5", "e7e5", 2, "fen-2", null, false, List.of());
        MoveNodeView e4 = new MoveNodeView(UUID.randomUUID(), "e4", "e2e4", 1, "fen-1", "Abre o centro", true, List.of(e5));
        when(getRepertoire.get(ID)).thenReturn(new RepertoireView(ID, "Italiana", Side.WHITE, List.of(e4)));

        mockMvc.perform(get("/api/repertoires/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Italiana"))
                .andExpect(jsonPath("$.roots[0].san").value("e4"))
                .andExpect(jsonPath("$.roots[0].userMove").value(true))
                .andExpect(jsonPath("$.roots[0].comment").value("Abre o centro"))
                .andExpect(jsonPath("$.roots[0].children[0].uci").value("e7e5"))
                .andExpect(jsonPath("$.roots[0].children[0].fenAfter").value("fen-2"));
    }

    @Test
    @DisplayName("GET de repertório inexistente devolve 404 em ProblemDetail")
    void unknownRepertoireReturns404() throws Exception {
        when(getRepertoire.get(ID)).thenThrow(new RepertoireNotFoundException(ID));

        mockMvc.perform(get("/api/repertoires/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Repertório não encontrado"));
    }

    @Test
    @DisplayName("GET com id que não é UUID devolve 400")
    void invalidIdReturns400() throws Exception {
        mockMvc.perform(get("/api/repertoires/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST de linha devolve a quantidade de cards criados")
    void addsLine() throws Exception {
        when(addLine.addLine(new AddLineCommand(ID, List.of("e4", "e5", "Nf3")))).thenReturn(new AddLineResult(2));

        mockMvc.perform(post("/api/repertoires/{id}/lines", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'moves':['e4','e5','Nf3']}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdCards").value(2));
    }

    @Test
    @DisplayName("POST de linha vazia devolve 400")
    void rejectsEmptyLine() throws Exception {
        mockMvc.perform(post("/api/repertoires/{id}/lines", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'moves':[]}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.moves").exists());
    }

    @Test
    @DisplayName("conflito no repertório devolve 409")
    void conflictReturns409() throws Exception {
        when(addLine.addLine(any())).thenThrow(new ConflictingRepertoireMoveException("Nf3", "Bc4", 3));

        mockMvc.perform(post("/api/repertoires/{id}/lines", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'moves':['e4','e5','Bc4']}")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito no repertório"));
    }

    @Test
    @DisplayName("lance ilegal devolve 422")
    void illegalMoveReturns422() throws Exception {
        when(addLine.addLine(any())).thenThrow(new IllegalMoveException("Ke2", "start"));

        mockMvc.perform(post("/api/repertoires/{id}/lines", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'moves':['Ke2']}")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O lance Ke2 não é válido na posição start"));
    }

    @Test
    @DisplayName("POST de PGN devolve linhas importadas e cards criados")
    void importsPgn() throws Exception {
        when(importPgn.importPgn(new ImportPgnCommand(ID, "1. e4 e5"))).thenReturn(new ImportPgnResult(1, 1));

        mockMvc.perform(post("/api/repertoires/{id}/pgn", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'pgn':'1. e4 e5'}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedLines").value(1))
                .andExpect(jsonPath("$.createdCards").value(1));
    }

    @Test
    @DisplayName("PGN inválido devolve 422")
    void invalidPgnReturns422() throws Exception {
        when(importPgn.importPgn(any())).thenThrow(new InvalidPgnException("O PGN não tem lances"));

        mockMvc.perform(post("/api/repertoires/{id}/pgn", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("{'pgn':'*'}")))
                .andExpect(status().isUnprocessableContent());
    }
}
