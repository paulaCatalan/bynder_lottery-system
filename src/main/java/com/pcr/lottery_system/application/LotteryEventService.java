package com.pcr.lottery_system.application;

import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.domain.repository.LotteryEventRepository;
import com.pcr.lottery_system.infrastructure.dto.ParticipateInLotteryCommand;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class LotteryEventService {
    private final LotteryEventRepository lotteryEventRepository;

    public LotteryEventService(LotteryEventRepository lotteryEventRepository) {
        this.lotteryEventRepository = lotteryEventRepository;
    }

    @Scheduled(cron = "0 0 9 * * *")
    void startDailyLottery() {
        System.out.println("Attempting to start a new daily lottery event...");

        LocalDateTime now = LocalDateTime.now();

        Instant startTime = now.atZone(ZoneId.systemDefault())
                .toInstant();

        Instant endTime = now.withHour(23).withMinute(59).withSecond(59).withNano(0)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        LotteryEvent newDailyLottery = LotteryEvent.createLottery(UUID.randomUUID().toString(), startTime, endTime);

        lotteryEventRepository.save(newDailyLottery);
        System.out.println("New daily lottery event started: ID=" + newDailyLottery.id() +
                ", Start=" + newDailyLottery.startTime() +
                ", End=" + newDailyLottery.endTime() +
                ", Status=" + newDailyLottery.status());
    }

    @Scheduled(cron = "0 0 0 * * *")
    void closeLotteryEventsAtMidnight() {
        System.out.println("Attempting to open lottery events...");

        List<LotteryEvent> openLotteries = lotteryEventRepository.findLotteryEventByStatus(LotteryStatus.OPEN);
        //Handle open lotteries being empty list with TDD
        System.out.println("Found " + openLotteries.size() + " OPEN lottery events to close.");
        if (openLotteries != null && !openLotteries.isEmpty()) {
            for (LotteryEvent openLottery : openLotteries) {
                LotteryEvent closedLottery = openLottery.close();
                lotteryEventRepository.save(closedLottery);
            }
        }
    }

    public List<LotteryEvent> findLotteryEventsByStatus(LotteryStatus status) {
        return lotteryEventRepository.findLotteryEventByStatus(status);
    }

    public Ballot participateInLotteryEvent(ParticipateInLotteryCommand participateInLotteryCommand) {
        LotteryEvent lotteryEvent = lotteryEventRepository.findLotteryEventById(participateInLotteryCommand.lotteryId());
        if (lotteryEvent.status() == LotteryStatus.OPEN) {
            String newBallotId = UUID.randomUUID().toString();
            Ballot newBallot = new Ballot(
                    newBallotId,
                    participateInLotteryCommand.lotteryId(),
                    participateInLotteryCommand.participantId()
            );
            // Call repository to save the new ballot
            return newBallot;
        }
        return null;
    }
}
