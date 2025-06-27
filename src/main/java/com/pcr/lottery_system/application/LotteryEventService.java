package com.pcr.lottery_system.application;

import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.repository.LotteryEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class LotteryEventService {
    private final LotteryEventRepository lotteryEventRepository;

    public LotteryEventService(LotteryEventRepository lotteryEventRepository) {
        this.lotteryEventRepository = lotteryEventRepository;
    }

    @Scheduled(cron = "0 0 9 * * *")
    void startDailyLottery() {
        LocalDateTime now = LocalDateTime.now(); // Gets current date and time in system's default timezone

        Instant startTime = now.atZone(ZoneId.systemDefault())
                .toInstant();

        Instant endTime = now.withHour(23).withMinute(59).withSecond(59).withNano(0)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        LotteryEvent newDailyLottery = LotteryEvent.createLottery(UUID.randomUUID().toString(), startTime, endTime);

        lotteryEventRepository.save(newDailyLottery);
    }
}
