package com.pcr.lottery_system.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pcr.lottery_system.domain.model.LotteryStatus;

public record LotteryEventJson(
        @JsonProperty("lottery_id") String id,
        @JsonProperty("start_date") String startTime,
        @JsonProperty("end_date") String endTime,
        LotteryStatus status,
        @JsonProperty("winner_ballot_id") String winnerBallotId
) {
}
