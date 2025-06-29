package com.pcr.lottery_system.infrastructure.converter;

import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.infrastructure.dto.BallotJson;
import org.springframework.stereotype.Component;

@Component
public class BallotConverter {
    public BallotJson toDto(Ballot ballot) {
        if (ballot == null) {
            return null;
        }
        return new BallotJson(
                ballot.ballotId(),
                ballot.lotteryId(),
                ballot.participantId()
        );
    }
    public Ballot toDomain(BallotJson ballotJson) {
        if (ballotJson == null) {
            return null;
        }
        return new Ballot(
                ballotJson.ballotId(),
                ballotJson.lotteryEventId(),
                ballotJson.participantId()
        );
    }
}