package com.pcr.lottery_system.domain.model;

import java.time.Instant;

public record LotteryEvent(
        String id,
        Instant startTime,
        Instant endTime,
        LotteryStatus status,
        String winnerBallotId
) {
    public LotteryEvent {
        if (id == null) {
            throw new IllegalArgumentException("LotteryEvent ID cannot be null.");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("LotteryEvent startTime cannot be null.");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("LotteryEvent endTime cannot be null.");
        }
        if (status == null) {
            throw new IllegalArgumentException("LotteryEvent status cannot be null.");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("LotteryEvent startTime cannot be after endTime.");
        }
        if (status == LotteryStatus.DRAWN && winnerBallotId == null) {
            throw new IllegalArgumentException("Winner ballot must be specified for a DRAWN lottery event.");
        }
        if (status != LotteryStatus.DRAWN && winnerBallotId != null) {
            throw new IllegalArgumentException("Winner ballot cannot be specified for a lottery event not in DRAWN status.");
        }
    }

    public static LotteryEvent createLottery(String id, Instant startTime, Instant endTime) {
        return new LotteryEvent(id, startTime, endTime, LotteryStatus.OPEN, null);
    }
}