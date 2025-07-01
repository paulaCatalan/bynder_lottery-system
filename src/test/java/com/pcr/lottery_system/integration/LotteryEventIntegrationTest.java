package com.pcr.lottery_system.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcr.lottery_system.LotterySystemApplication;
import com.pcr.lottery_system.application.LotteryEventService;
import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.infrastructure.dto.LotteryParticipationRequest;
import com.pcr.lottery_system.infrastructure.dto.ParticipantRegistrationRequest;
import com.pcr.lottery_system.infrastructure.dto.ParticipateInLotteryCommand;
import com.pcr.lottery_system.infrastructure.persistence.JsonBallotRepository;
import com.pcr.lottery_system.infrastructure.persistence.JsonLotteryEventRepository;
import com.pcr.lottery_system.infrastructure.persistence.JsonParticipantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LotterySystemApplication.class)
@AutoConfigureMockMvc
class LotteryEventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JsonParticipantRepository jsonParticipantRepository;
    @Autowired
    private JsonLotteryEventRepository jsonLotteryEventRepository;
    @Autowired
    private JsonBallotRepository jsonBallotRepository;
    @Autowired
    private LotteryEventService lotteryEventService;

    @BeforeEach
    void setUp() {
        // Clear all JSON files before each test to ensure a clean state
        jsonParticipantRepository.clearFile();
        jsonLotteryEventRepository.clearFile();
        jsonBallotRepository.clearFile();
    }

    @AfterEach
    void tearDown() {
        jsonParticipantRepository.clearFile();
        jsonLotteryEventRepository.clearFile();
        jsonBallotRepository.clearFile();
    }

    @Test
    void shouldParticipateInLotterySuccessfully() throws Exception {
        ParticipantRegistrationRequest participantRequest = new ParticipantRegistrationRequest("alice@example.com", "Alice");
        MvcResult participantResult = mockMvc.perform(post("/api/participant/register") // Corrected URI
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participantRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        String participantId = objectMapper.readTree(participantResult.getResponse().getContentAsString()).get("participantId").asText();

        String lotteryId = UUID.randomUUID().toString();
        LotteryEvent openLottery = LotteryEvent.createLottery(
                lotteryId,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        jsonLotteryEventRepository.save(openLottery);

        LotteryParticipationRequest participationRequest = new LotteryParticipationRequest(
                lotteryId,
                participantId);
        mockMvc.perform(post("/api/lotteries/participate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ballotId").exists())
                .andExpect(jsonPath("$.message").value("Participation submitted successfully"));

        assertNotNull(jsonLotteryEventRepository.findLotteryEventById(lotteryId));
    }

    @Test
    void shouldFailToParticipateInNonExistentLottery() throws Exception {
        UUID nonExistentLotteryId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        LotteryParticipationRequest participationRequest = new LotteryParticipationRequest(
                nonExistentLotteryId.toString(),
                participantId.toString()
        );
        mockMvc.perform(post("/api/lotteries/participate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participationRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailToParticipateInClosedLottery() throws Exception {
        ParticipantRegistrationRequest participantRequest = new ParticipantRegistrationRequest("bob@example.com", "Bob");
        MvcResult participantResult = mockMvc.perform(post("/api/participant/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participantRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        UUID participantId = UUID.fromString(objectMapper.readTree(participantResult.getResponse().getContentAsString()).get("participantId").asText());

        UUID lotteryId = UUID.randomUUID();
        LotteryEvent closedLottery = LotteryEvent.createLottery(
                lotteryId.toString(),
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS)
        ).close();
        jsonLotteryEventRepository.save(closedLottery);

        LotteryParticipationRequest participationRequest = new LotteryParticipationRequest(
                lotteryId.toString(),
                participantId.toString()
        );
        mockMvc.perform(post("/api/lotteries/participate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participationRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailToParticipateWithNonExistentParticipant() throws Exception {
        UUID lotteryId = UUID.randomUUID();
        LotteryEvent openLottery = LotteryEvent.createLottery(
                lotteryId.toString(),
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(1, ChronoUnit.HOURS)
        );
        jsonLotteryEventRepository.save(openLottery);

        UUID nonExistentParticipantId = UUID.randomUUID();
        LotteryParticipationRequest participationRequest = new LotteryParticipationRequest(
                lotteryId.toString(),
                nonExistentParticipantId.toString()
        );
        mockMvc.perform(post("/api/lotteries/participate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(participationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Participant ID " + nonExistentParticipantId + " not found."));
    }

    @Test
    void shouldGetDrawnLotteryDetailsSuccessfully() throws Exception {
        LocalDate drawnDate = LocalDate.now().minusDays(1); // Yesterday
        UUID lotteryId1 = UUID.randomUUID();
        UUID winningBallotId1 = UUID.randomUUID();
        LotteryEvent drawnLottery1 = new LotteryEvent(
                lotteryId1.toString(),
                drawnDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                drawnDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant(),
                LotteryStatus.DRAWN,
                winningBallotId1.toString()
        );
        jsonLotteryEventRepository.save(drawnLottery1);

        UUID lotteryId2 = UUID.randomUUID();
        UUID winningBallotId2 = UUID.randomUUID();
        LotteryEvent drawnLottery2 = new LotteryEvent(
                lotteryId2.toString(),
                drawnDate.atStartOfDay(ZoneId.systemDefault()).plusHours(1).toInstant(), // Slightly different start time
                drawnDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant(),
                LotteryStatus.DRAWN,
                winningBallotId2.toString()
        );
        jsonLotteryEventRepository.save(drawnLottery2);

        mockMvc.perform(get("/api/lotteries/drawn/" + drawnDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) // Assert that the root is an array
                .andExpect(jsonPath("$.length()").value(2)) // Assert the array size
                .andExpect(jsonPath("$[0].id").value(lotteryId1.toString())) // Access first element
                .andExpect(jsonPath("$[0].status").value("DRAWN"))
                .andExpect(jsonPath("$[0].winnerBallotId").value(winningBallotId1.toString()))
                .andExpect(jsonPath("$[1].id").value(lotteryId2.toString())) // Access second element
                .andExpect(jsonPath("$[1].status").value("DRAWN"))
                .andExpect(jsonPath("$[1].winnerBallotId").value(winningBallotId2.toString()));
    }

    @Test
    void shouldFailToGetDrawnLotteryWhenNoneExistsForDate() throws Exception {
        LocalDate nonExistentDate = LocalDate.now().minusDays(5);

        mockMvc.perform(get("/lotteries/drawn/" + nonExistentDate))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFailToGetDrawnLotteryWhenExistsButNotDrawn() throws Exception {
        LocalDate closedDate = LocalDate.now().minusDays(2);
        UUID lotteryId = UUID.randomUUID();
        LotteryEvent closedLottery = new LotteryEvent(
                lotteryId.toString(),
                closedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                closedDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant(),
                LotteryStatus.CLOSED,
                null
        );
        jsonLotteryEventRepository.save(closedLottery);

        mockMvc.perform(get("/lotteries/drawn/" + closedDate.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldStartDailyLotteryAutomatically() {
        lotteryEventService.startDailyLottery();

        List<LotteryEvent> openLotteries = jsonLotteryEventRepository.findLotteryEventByStatus(LotteryStatus.OPEN);
        assertFalse(openLotteries.isEmpty());
        assertEquals(1, openLotteries.size());
        assertEquals(LotteryStatus.OPEN, openLotteries.get(0).status());
        assertNotNull(openLotteries.get(0).startTime());
        assertNotNull(openLotteries.get(0).endTime());
    }

    @Test
    void shouldCloseLotteryAndDrawWinnerAutomatically() throws Exception {
        LocalDate today = LocalDate.now();
        UUID lotteryId = UUID.randomUUID();
        LotteryEvent openLottery = LotteryEvent.createLottery(
                lotteryId.toString(),
                today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
        );
        jsonLotteryEventRepository.save(openLottery);

        UUID participantId1 = UUID.randomUUID();
        UUID participantId2 = UUID.randomUUID();
        jsonParticipantRepository.save(new Participant(participantId1.toString(), "p1@test.com", "Participant One"));
        jsonParticipantRepository.save(new Participant(participantId2.toString(), "p2@test.com", "Participant Two"));

        lotteryEventService.participateInLotteryEvent(new ParticipateInLotteryCommand(lotteryId.toString(), participantId1.toString()));
        lotteryEventService.participateInLotteryEvent(new ParticipateInLotteryCommand(lotteryId.toString(), participantId2.toString()));

        lotteryEventService.closeLotteryEventsAtMidnight();

        LotteryEvent closedAndDrawnLottery = jsonLotteryEventRepository.findLotteryEventById(lotteryId.toString());
        assertNotNull(closedAndDrawnLottery);
        assertEquals(LotteryStatus.DRAWN, closedAndDrawnLottery.status());
        assertNotNull(closedAndDrawnLottery.winnerBallotId());
    }

    @Test
    void shouldCloseLotteryWithoutWinnerIfNoBallots() {
        LocalDate today = LocalDate.now();
        UUID lotteryId = UUID.randomUUID();
        LotteryEvent openLottery = LotteryEvent.createLottery(
                lotteryId.toString(),
                today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
        );
        jsonLotteryEventRepository.save(openLottery);

        lotteryEventService.closeLotteryEventsAtMidnight();

        LotteryEvent closedLottery = jsonLotteryEventRepository.findLotteryEventById(lotteryId.toString());
        assertNotNull(closedLottery);
        assertEquals(LotteryStatus.CLOSED, closedLottery.status());
        assertNull(closedLottery.winnerBallotId());
    }

}
