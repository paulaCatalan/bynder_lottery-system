package com.pcr.lottery_system.infrastructure.converter;

import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.infrastructure.dto.LotteryEventJson;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class LotteryEventConverter {

    public LotteryEventJson toDto(LotteryEvent lotteryEvent) {
        if (lotteryEvent == null) {
            return null;
        }
        return new LotteryEventJson(
                lotteryEvent.id(),
                lotteryEvent.startTime().toString(),
                lotteryEvent.endTime().toString(),
                lotteryEvent.status(),
                lotteryEvent.winnerBallotId() != null ? lotteryEvent.winnerBallotId() : null
        );
    }

    public LotteryEvent toDomain(LotteryEventJson lotteryEventJson) {
        if (lotteryEventJson == null) {
            return null;
        }
        return new LotteryEvent(
                lotteryEventJson.id(),
                Instant.parse(lotteryEventJson.startTime()),
                Instant.parse(lotteryEventJson.endTime()),
                lotteryEventJson.status(),
                lotteryEventJson.winnerBallotId() != null ? lotteryEventJson.winnerBallotId() : null
        );
    }
}
