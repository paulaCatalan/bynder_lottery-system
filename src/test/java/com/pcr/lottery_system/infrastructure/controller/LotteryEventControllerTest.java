package com.pcr.lottery_system.infrastructure.controller;

import com.pcr.lottery_system.application.LotteryEventService;
import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.infrastructure.dto.LotteryEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class LotteryEventControllerTest {

    private LotteryEventController lotteryEventController;

    @Mock
    private LotteryEventService lotteryEventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lotteryEventController = new LotteryEventController(lotteryEventService);
    }

    @Test
    void shouldReturnActiveLotteries() {
        String lotteryId1 = UUID.randomUUID().toString();
        Instant startTime1 = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant endTime1 = Instant.now().plus(1, ChronoUnit.HOURS);
        LotteryEvent lotteryEvent1 = new LotteryEvent(lotteryId1, startTime1, endTime1, LotteryStatus.OPEN, null);

        String lotteryId2 = UUID.randomUUID().toString();
        Instant startTime2 = Instant.now().minus(30, ChronoUnit.MINUTES);
        Instant endTime2 = Instant.now().plus(30, ChronoUnit.MINUTES);
        LotteryEvent lotteryEvent2 = new LotteryEvent(lotteryId2, startTime2, endTime2, LotteryStatus.OPEN, null);

        List<LotteryEvent> mockOpenLotteries = Arrays.asList(lotteryEvent1, lotteryEvent2);

        when(lotteryEventService.findLotteryEventsByStatus(LotteryStatus.OPEN))
                .thenReturn(mockOpenLotteries);

        LotteryEventResponse response1 = new LotteryEventResponse(lotteryId1, startTime1, endTime1, LotteryStatus.OPEN, null);
        LotteryEventResponse response2 = new LotteryEventResponse(lotteryId2, startTime2, endTime2, LotteryStatus.OPEN, null);
        List<LotteryEventResponse> expectedResponses = Arrays.asList(response1, response2);

        ResponseEntity<List<LotteryEventResponse>> responseEntity = lotteryEventController.getOpenLotteries();

        verify(lotteryEventService, times(1)).findLotteryEventsByStatus(LotteryStatus.OPEN);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        assertNotNull(responseEntity.getBody());
        assertEquals(expectedResponses.size(), responseEntity.getBody().size());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveLotteries() {
        when(lotteryEventService.findLotteryEventsByStatus(LotteryStatus.OPEN))
                .thenReturn(List.of());

        ResponseEntity<List<LotteryEventResponse>> responseEntity = lotteryEventController.getOpenLotteries();

        verify(lotteryEventService, times(1)).findLotteryEventsByStatus(LotteryStatus.OPEN);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(0, responseEntity.getBody().size());
        assertEquals(List.of(), responseEntity.getBody());
    }
}
