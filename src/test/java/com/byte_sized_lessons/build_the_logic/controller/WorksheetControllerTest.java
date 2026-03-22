package com.byte_sized_lessons.build_the_logic.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WorksheetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGenerateWorksheetFromTemplate() throws Exception {
        mockMvc.perform(post("/api/worksheets/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "topic": "loops",
                      "difficulty": "basic",
                      "numQuestions": 5
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metadata.title").value("Loops Worksheet (Basic)"))
            .andExpect(jsonPath("$.questions.length()").value(5))
            .andExpect(jsonPath("$.questions[0].type").value("FILL_IN_STEPS"))
            .andExpect(jsonPath("$.questions[0].content.template").value("fill_in_steps"))
            .andExpect(jsonPath("$.answerKey.entries.length()").value(5));
    }

    @Test
    void shouldSaveAndExportWorksheetAsPdf() throws Exception {
        String response = mockMvc.perform(post("/api/worksheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "topic": "loops",
                      "difficulty": "basic",
                      "numQuestions": 3
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode payload = objectMapper.readTree(response);
        long id = payload.get("id").asLong();

        mockMvc.perform(get("/api/worksheets/{worksheetId}/export", id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
