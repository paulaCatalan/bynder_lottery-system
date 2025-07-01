package com.pcr.lottery_system.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcr.lottery_system.LotterySystemApplication;
import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.infrastructure.dto.ParticipantRegistrationRequest;
import com.pcr.lottery_system.infrastructure.persistence.JsonParticipantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LotterySystemApplication.class)
@AutoConfigureMockMvc
public class ParticipantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JsonParticipantRepository jsonParticipantRepository;

    @BeforeEach
    void setUp() {
        jsonParticipantRepository.clearFile();
    }

    @AfterEach
    void tearDown() {
        jsonParticipantRepository.clearFile();
    }

    @Test
    void shouldRegisterParticipantSuccessfully() throws Exception {
        ParticipantRegistrationRequest request = new ParticipantRegistrationRequest("test@example.com", "Test User");

        mockMvc.perform(post("/api/participant/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participantId").exists())
                .andExpect(jsonPath("$.message").value("Participant registered successfully"));

        Participant foundParticipant = jsonParticipantRepository.findByEmail("test@example.com");
        assertNotNull(foundParticipant);
        assertEquals("test@example.com", foundParticipant.email());
        assertEquals("Test User", foundParticipant.name());
    }

    @Test
    void shouldFailToRegisterParticipantWithDuplicateEmail() throws Exception {
        ParticipantRegistrationRequest request = new ParticipantRegistrationRequest("duplicate@example.com", "First User");
        mockMvc.perform(post("/api/participant/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/participant/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()) // Expect 409 Conflict
                .andExpect(jsonPath("$.message").value("Participant with email 'duplicate@example.com' is already registered."));
    }

}
