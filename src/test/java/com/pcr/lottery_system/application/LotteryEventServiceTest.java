package com.pcr.lottery_system.application;

import com.pcr.lottery_system.domain.exception.InvalidLotteryEventStatusException;
import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import com.pcr.lottery_system.infrastructure.dto.ParticipateInLotteryCommand;
import com.pcr.lottery_system.infrastructure.persistance.JsonBallotRepository;
import com.pcr.lottery_system.infrastructure.persistance.JsonLotteryEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class LotteryEventServiceTest {
    @Mock
    JsonLotteryEventRepository jsonLotteryEventRepository;

    @Mock
    JsonBallotRepository jsonBallotRepository;

    @Mock
    ParticipantRepository participantRepository;

    LotteryEventService lotteryEventService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        lotteryEventService = new LotteryEventService(jsonLotteryEventRepository, jsonBallotRepository, participantRepository);
    }

    @Test
    void shouldStartDailyLottery(){
        lotteryEventService.startDailyLottery();
        verify(jsonLotteryEventRepository).save(any()); //TODO: Testing with any because I don't want to handle UUIDs
    }

    @Test
    void shouldCloseOpenLotteryEventsAtMidnightAndDrawAWinnerBallot(){
        LotteryEvent openLotteryEvent = LotteryEvent.createLottery(
                "testId",
                Instant.now(),
                Instant.now().plus(15, ChronoUnit.HOURS)
        );
        LotteryEvent closedLotteryEvent = openLotteryEvent.close();
        LotteryEvent drawnLotteryEvent = closedLotteryEvent.drawWinner("ballotId");
        Participant participant = new Participant("participantId", "email", "Test");

        when(jsonLotteryEventRepository.findLotteryEventByStatus(LotteryStatus.OPEN)).thenReturn(List.of(openLotteryEvent));
        when(participantRepository.findById("participantId")).thenReturn(participant);
        when(jsonBallotRepository.findAllBallotsOfALottery("testId")).thenReturn(
                List.of(
                        new Ballot("ballotId", "testId", "participantId"))
        );

        lotteryEventService.closeLotteryEventsAtMidnight();

        verify(jsonLotteryEventRepository).findLotteryEventByStatus(LotteryStatus.OPEN);
        verify(jsonBallotRepository).findAllBallotsOfALottery("testId");
        verify(jsonLotteryEventRepository).save(closedLotteryEvent);
        verify(jsonLotteryEventRepository).save(drawnLotteryEvent);
    }

    @Test
    void shouldCloseOpenLotteryEventsAndDrawAWinnerFromMultipleBallots() {
        String lotteryId = UUID.randomUUID().toString();
        LotteryEvent openLotteryEvent = LotteryEvent.createLottery(
                lotteryId,
                Instant.now().minus(2, ChronoUnit.HOURS),
                Instant.now().minus(1, ChronoUnit.HOURS)
        );

        String participantId1 = UUID.randomUUID().toString();
        String participantId2 = UUID.randomUUID().toString();

        Ballot ballot1 = new Ballot("ballot1", participantId1, lotteryId);
        Ballot ballot2 = new Ballot("ballot2", participantId1, lotteryId);
        Ballot ballot3 = new Ballot("ballot3", participantId2, lotteryId);

        List<Ballot> allBallots = Arrays.asList(ballot1, ballot2, ballot3);
        List<String> allBallotIds = Arrays.asList(ballot1.ballotId(), ballot2.ballotId(), ballot3.ballotId());


        when(jsonLotteryEventRepository.findLotteryEventByStatus(LotteryStatus.OPEN)).thenReturn(List.of(openLotteryEvent));
        when(jsonLotteryEventRepository.findLotteryEventById(lotteryId)).thenReturn(openLotteryEvent);
        when(jsonBallotRepository.findAllBallotsOfALottery(lotteryId)).thenReturn(allBallots);

        lotteryEventService.closeLotteryEventsAtMidnight();

        verify(jsonLotteryEventRepository, times(1)).findLotteryEventByStatus(LotteryStatus.OPEN);
        verify(jsonBallotRepository, times(1)).findAllBallotsOfALottery(lotteryId);

        ArgumentCaptor<LotteryEvent> lotteryEventCaptor = ArgumentCaptor.forClass(LotteryEvent.class);
        verify(jsonLotteryEventRepository, times(2)).save(lotteryEventCaptor.capture());

        List<LotteryEvent> savedLotteries = lotteryEventCaptor.getAllValues();
        assertEquals(2, savedLotteries.size());

        LotteryEvent firstSave = savedLotteries.get(0);
        assertEquals(lotteryId, firstSave.id());
        assertEquals(LotteryStatus.CLOSED, firstSave.status());
        assertNull(firstSave.winnerBallotId());

        LotteryEvent secondSave = savedLotteries.get(1);
        assertEquals(lotteryId, secondSave.id());
        assertEquals(LotteryStatus.DRAWN, secondSave.status());
        assertNotNull(secondSave.winnerBallotId());
        assertTrue(allBallotIds.contains(secondSave.winnerBallotId()),
                "Winning ballot ID should be one of the provided ballot IDs.");
    }

    @Test
    void shouldKeepLotteryInClosedStateIfThereAreNoBallotsToWin(){
        LotteryEvent openLotteryEvent = LotteryEvent.createLottery(
                "testId",
                Instant.now(),
                Instant.now().plus(15, ChronoUnit.HOURS)
        );
        LotteryEvent closedLotteryEvent = openLotteryEvent.close();
        LotteryEvent drawnLotteryEvent = closedLotteryEvent.drawWinner("ballotId");
        when(jsonLotteryEventRepository.findLotteryEventByStatus(LotteryStatus.OPEN)).thenReturn(List.of(openLotteryEvent));
        when(jsonBallotRepository.findAllBallotsOfALottery("testId")).thenReturn(List.of());

        lotteryEventService.closeLotteryEventsAtMidnight();

        verify(jsonLotteryEventRepository).save(closedLotteryEvent);
        verify(jsonLotteryEventRepository, never()).save(drawnLotteryEvent);

    }

    @Test
    void shouldNotCloseOpenLotteryEventsAtMidnightIfThereAreNoOpenLotteries(){

        when(jsonLotteryEventRepository.findLotteryEventByStatus(LotteryStatus.OPEN)).thenReturn(List.of());

        lotteryEventService.closeLotteryEventsAtMidnight();

        verify(jsonLotteryEventRepository).findLotteryEventByStatus(LotteryStatus.OPEN);
        verify(jsonLotteryEventRepository, never()).save(any());

    }

    @Test
    void shouldSubmitParticipationInOpenLotteriesAndReturnNewBallot(){
        ParticipateInLotteryCommand command = new ParticipateInLotteryCommand("lotteryId", "participantId");
        LotteryEvent lotteryEvent = new LotteryEvent(
                "lotteryId",
                Instant.now().minus(15, ChronoUnit.MINUTES),
                Instant.now().plus(14, ChronoUnit.HOURS),
                LotteryStatus.OPEN,
                null
                );
        Participant participant = new Participant("participantId", "email", "Participant");
        when(jsonLotteryEventRepository.findLotteryEventById("lotteryId")).thenReturn(lotteryEvent);
        when(participantRepository.findById("participantId")).thenReturn(participant);
        Ballot ballot = lotteryEventService.participateInLotteryEvent(command);

        verify(jsonLotteryEventRepository).findLotteryEventById("lotteryId");
        verify(jsonBallotRepository).save(ballot);
        assertEquals(ballot.participantId(), command.participantId());
        assertEquals(ballot.lotteryId(), command.lotteryId());
        Assertions.assertNotNull(ballot.ballotId());
    }

    @Test
    void shouldNotSubmitParticipationInLotteriesThatAreNotOpenAndThrowException() {
        UUID lotteryId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        LotteryEvent closedLotteryEvent = new LotteryEvent(
                lotteryId.toString(),
                Instant.now().minus(15, ChronoUnit.MINUTES),
                Instant.now().plus(14, ChronoUnit.HOURS),
                LotteryStatus.CLOSED,
                null
        );
        Participant participant = new Participant(participantId.toString(), "email@example.com", "Test Participant");
        when(jsonLotteryEventRepository.findLotteryEventById(lotteryId.toString())).thenReturn(closedLotteryEvent);
        when(participantRepository.findById(participantId.toString())).thenReturn(participant);


        ParticipateInLotteryCommand command =
                new ParticipateInLotteryCommand(lotteryId.toString(), participantId.toString());

        assertThrows(InvalidLotteryEventStatusException.class, () -> {
            lotteryEventService.participateInLotteryEvent(command);
        });
        verify(jsonBallotRepository, never()).save(any(Ballot.class));
    }

    @Test
    void shouldReturnOnlyDrawnEventsForADate(){
        LocalDate testDate = LocalDate.of(2025, 7, 1);
        String lotteryId = UUID.randomUUID().toString();
        String winnerBallotId = UUID.randomUUID().toString();
        LotteryEvent drawnLottery = new LotteryEvent(
                lotteryId,
                testDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                testDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant(),
                LotteryStatus.DRAWN,
                winnerBallotId
        );
        LotteryEvent closedLottery = new LotteryEvent(
                UUID.randomUUID().toString(),
                testDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                testDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant(),
                LotteryStatus.CLOSED,
                null
        );
        List<LotteryEvent> lotteriesForDate = Arrays.asList(closedLottery, drawnLottery);
        when(jsonLotteryEventRepository.findLotteryEventByEndLotteryDate(testDate)).thenReturn(lotteriesForDate);

       List<LotteryEvent> resultList = lotteryEventService.findDrawnLotteriesByEndLotteryDate(testDate);

        // Then
        assertNotNull(resultList);
        assertEquals(List.of(drawnLottery), resultList);
        verify(jsonLotteryEventRepository, times(1)).findLotteryEventByEndLotteryDate(testDate);
    }


}