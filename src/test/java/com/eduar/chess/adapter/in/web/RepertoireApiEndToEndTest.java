package com.eduar.chess.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RepertoireApiEndToEndTest {

    private static final String ITALIAN_PGN = """
            [Event "Repertório"]
            1. e4 e5 2. Nf3 Nc6 (2... Nf6 3. d3) 3. Bc4 *
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("cria repertório, importa PGN com variantes e lê a árvore de volta")
    void createImportAndRead() throws Exception {
        String created = mockMvc.perform(post("/api/repertoires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Italiana\",\"side\":\"WHITE\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mockMvc.perform(post("/api/repertoires/{id}/pgn", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pgn\":" + quote(ITALIAN_PGN) + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedLines").value(2))
                .andExpect(jsonPath("$.createdCards").value(4));

        mockMvc.perform(get("/api/repertoires/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roots[0].san").value("e4"))
                .andExpect(jsonPath("$.roots[0].uci").value("e2e4"))
                .andExpect(jsonPath("$.roots[0].userMove").value(true))
                .andExpect(jsonPath("$.roots[0].children[0].children[0].san").value("Nf3"))
                .andExpect(jsonPath("$.roots[0].children[0].children[0].children.length()").value(2));
    }

    @Test
    @DisplayName("lance ilegal numa linha real devolve 422")
    void illegalMoveReturns422() throws Exception {
        String created = mockMvc.perform(post("/api/repertoires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Teste\",\"side\":\"WHITE\"}"))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mockMvc.perform(post("/api/repertoires/{id}/lines", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moves\":[\"e4\",\"e5\",\"Ke3\"]}"))
                .andExpect(status().isUnprocessableContent());
    }

    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
