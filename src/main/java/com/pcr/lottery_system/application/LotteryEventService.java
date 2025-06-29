package com.pcr.lottery_system.application;

import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.domain.repository.BallotRepository;
import com.pcr.lottery_system.domain.repository.LotteryEventRepository;
import com.pcr.lottery_system.infrastructure.dto.ParticipateInLotteryCommand;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class LotteryEventService {
    private final LotteryEventRepository lotteryEventRepository;
    private final BallotRepository ballotRepository;
    private final Random random = new Random();


    public LotteryEventService(
            LotteryEventRepository lotteryEventRepository,
            BallotRepository ballotRepository
    ) {
        this.lotteryEventRepository = lotteryEventRepository;
        this.ballotRepository = ballotRepository;
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
        System.out.println("Found " + openLotteries.size() + " OPEN lottery events to close.");
        if (openLotteries != null && !openLotteries.isEmpty()) {
            for (LotteryEvent lotteryEvent : openLotteries) {
                LotteryEvent closedLottery = lotteryEvent.close();
                lotteryEventRepository.save(closedLottery);

                Ballot winningBallot = selectWinnerForLottery(lotteryEvent.id());

                if (winningBallot != null) {
                    LotteryEvent drawnLotteryEvent = closedLottery.drawWinner(winningBallot.ballotId());
                    lotteryEventRepository.save(drawnLotteryEvent);
                }
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
            ballotRepository.save(newBallot);
            return newBallot;
        }
        return null;
    }

    private Ballot selectWinnerForLottery(String lotteryId){
        List<Ballot> allBallotsForLottery = ballotRepository.findAllBallotsOfALottery(lotteryId);
        if (allBallotsForLottery.isEmpty()) {
            System.out.println("No ballots found for lottery event ID " + lotteryId + ". Cannot draw a winner for this lottery.");
            return null;
        }
        return allBallotsForLottery.get(random.nextInt(allBallotsForLottery.size()));
    }

    public List<LotteryEvent> findDrawnLotteriesByDate(LocalDate date) {
        return lotteryEventRepository.findLotteryEventByDate(date)
                .stream().filter(lotteryEvent -> lotteryEvent.status() == LotteryStatus.DRAWN).toList();
    }
}
