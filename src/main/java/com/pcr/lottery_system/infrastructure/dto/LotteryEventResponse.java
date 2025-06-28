package com.pcr.lottery_system.infrastructure.dto;

import com.pcr.lottery_system.domain.model.LotteryStatus;
import java.time.Instant;

public record LotteryEventResponse(
        String id,
        Instant startTime,
        Instant endTime,
        LotteryStatus status,
        String winnerBallotId
) {
}
