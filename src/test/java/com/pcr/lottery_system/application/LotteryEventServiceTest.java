package com.pcr.lottery_system.application;

import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.infrastructure.persistance.JsonLotteryEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
        verify(jsonLotteryEventRepository).save(closedLotteryEvent); //IDK If I need the update or I should save and that's all
    }

}