package com.pcr.lottery_system.application;

import com.pcr.lottery_system.domain.exception.InvalidLotteryEventStatusException;
import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.domain.repository.BallotRepository;
import com.pcr.lottery_system.domain.repository.LotteryEventRepository;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import com.pcr.lottery_system.infrastructure.dto.ParticipateInLotteryCommand;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;

@Service
public class LotteryEventService {
    private final LotteryEventRepository lotteryEventRepository;
    private final BallotRepository ballotRepository;
    private final ParticipantRepository participantRepository;
    private final Random random = new Random();


    public LotteryEventService(
            LotteryEventRepository lotteryEventRepository,
            BallotRepository ballotRepository,
            ParticipantRepository participantRepository
    ) {
        this.lotteryEventRepository = lotteryEventRepository;
        this.ballotRepository = ballotRepository;
        this.participantRepository = participantRepository;
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
        Participant participant = participantRepository.findById(participateInLotteryCommand.participantId());
        if (lotteryEvent == null) {
            throw new NoSuchElementException("Lottery event with ID " + participateInLotteryCommand.lotteryId() + " not found.");
        }
        if (participant == null) {
            throw new NoSuchElementException("Participant ID " + participateInLotteryCommand.participantId() + " not found.");
        }
        if (lotteryEvent.status() == LotteryStatus.OPEN) {
            String newBallotId = UUID.randomUUID().toString();
            Ballot newBallot = new Ballot(
                    newBallotId,
                    participateInLotteryCommand.lotteryId(),
                    participateInLotteryCommand.participantId()
            );
            ballotRepository.save(newBallot);
            return newBallot;
        } else {
            throw new InvalidLotteryEventStatusException("Lottery event with ID " + participateInLotteryCommand.lotteryId() + " is not open.");
        }
    }

    private Ballot selectWinnerForLottery(String lotteryId){
        List<Ballot> allBallotsForLottery = ballotRepository.findAllBallotsOfALottery(lotteryId);
        if (allBallotsForLottery.isEmpty()) {
            System.out.println("No ballots found for lottery event ID " + lotteryId + ". Cannot draw a winner for this lottery.");
            return null;
        }
        return allBallotsForLottery.get(random.nextInt(allBallotsForLottery.size()));
    }

    public List<LotteryEvent> findDrawnLotteriesByEndLotteryDate(LocalDate date) {
        return lotteryEventRepository.findLotteryEventByEndLotteryDate(date)
                .stream().filter(lotteryEvent -> lotteryEvent.status() == LotteryStatus.DRAWN).toList();
    }
}
