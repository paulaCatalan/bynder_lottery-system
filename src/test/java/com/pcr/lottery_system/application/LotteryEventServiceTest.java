package com.pcr.lottery_system.application;

import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.infrastructure.dto.ParticipateInLotteryCommand;
import com.pcr.lottery_system.infrastructure.persistance.JsonLotteryEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;


class LotteryEventServiceTest {
    @Mock
    JsonLotteryEventRepository jsonLotteryEventRepository;

    LotteryEventService lotteryEventService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        lotteryEventService = new LotteryEventService(jsonLotteryEventRepository);
    }

    @Test
    void shouldStartDailyLottery(){
        lotteryEventService.startDailyLottery();
        verify(jsonLotteryEventRepository).save(any()); //TODO: Testing with any because I don't want to handle UUIDs
    }

    @Test
    void shouldCloseOpenLotteryEventsAtMidnight(){
        LotteryEvent openLotteryEvent = LotteryEvent.createLottery(
                "testId",
                Instant.now(),
                Instant.now().plus(15, ChronoUnit.HOURS)
        );
        LotteryEvent closedLotteryEvent = openLotteryEvent.close();

        when(jsonLotteryEventRepository.findLotteryEventByStatus(LotteryStatus.OPEN)).thenReturn(List.of(openLotteryEvent));

        lotteryEventService.closeLotteryEventsAtMidnight();

        verify(jsonLotteryEventRepository).findLotteryEventByStatus(LotteryStatus.OPEN);
        verify(jsonLotteryEventRepository).save(closedLotteryEvent);
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
        when(jsonLotteryEventRepository.findLotteryEventById("lotteryId")).thenReturn(lotteryEvent);
        //Call ballot repository to save new ballot
        Ballot ballot = lotteryEventService.participateInLotteryEvent(command);

        verify(jsonLotteryEventRepository).findLotteryEventById("lotteryId");
        Assertions.assertEquals(ballot.participantId(), command.participantId());
        Assertions.assertEquals(ballot.lotteryId(), command.lotteryId());
        Assertions.assertNotNull(ballot.ballotId());
    }

    @Test
    void shouldNotSubmitParticipationInLotteriesThatAreNotOpenAndReturnNull(){
        ParticipateInLotteryCommand command = new ParticipateInLotteryCommand("lotteryId", "participantId");
        LotteryEvent lotteryEvent = new LotteryEvent(
                "lotteryId",
                Instant.now().minus(15, ChronoUnit.MINUTES),
                Instant.now().plus(14, ChronoUnit.HOURS),
                LotteryStatus.CLOSED,
                null
        );
        when(jsonLotteryEventRepository.findLotteryEventById("lotteryId")).thenReturn(lotteryEvent);
        //Call ballot repository to save new ballot
        Ballot ballot = lotteryEventService.participateInLotteryEvent(command);

        verify(jsonLotteryEventRepository).findLotteryEventById("lotteryId");
        Assertions.assertNull(ballot);
    }

}