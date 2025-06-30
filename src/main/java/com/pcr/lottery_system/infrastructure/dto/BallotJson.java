package com.pcr.lottery_system.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BallotJson(
        @JsonProperty("ballot_id") String ballotId,
        @JsonProperty("lottery_event_id") String lotteryEventId,
        @JsonProperty("participant_id") String participantId
) {
}
