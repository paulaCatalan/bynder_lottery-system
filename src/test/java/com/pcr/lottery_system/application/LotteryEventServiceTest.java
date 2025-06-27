package com.pcr.lottery_system.application;

import com.pcr.lottery_system.infrastructure.persistance.JsonLotteryEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

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

}