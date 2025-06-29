package com.pcr.lottery_system.infrastructure.converter;

import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.infrastructure.dto.BallotJson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.UUID;

class BallotConverterTest {

    BallotConverter ballotConverter = new BallotConverter();

    @Test
    void shouldConvertBallotJsonToBallot() {
        String id = UUID.randomUUID().toString();
        String participantId = UUID.randomUUID().toString();
        String lotteryEventId = UUID.randomUUID().toString();

        BallotJson ballotJson = new BallotJson(id, participantId, lotteryEventId);

        Ballot expectedBallot = new Ballot(id, participantId, lotteryEventId);

        Ballot convertedBallot = ballotConverter.toDomain(ballotJson);

        Assertions.assertNotNull(convertedBallot);
        Assertions.assertEquals(expectedBallot, convertedBallot);
    }

    @Test
    void shouldConvertBallotToBallotJson() {
        String id = UUID.randomUUID().toString();
        String participantId = UUID.randomUUID().toString();
        String lotteryEventId = UUID.randomUUID().toString();

        Ballot ballot = new Ballot(id, participantId, lotteryEventId);

        BallotJson expectedBallotJson = new BallotJson(id, participantId, lotteryEventId);

        BallotJson convertedBallotJson = ballotConverter.toDto(ballot);

        Assertions.assertNotNull(convertedBallotJson);
        Assertions.assertEquals(expectedBallotJson, convertedBallotJson);
    }

    @Test
    void shouldHandleNullBallotToDtoConversion() {
        BallotJson converted = ballotConverter.toDto(null);
        Assertions.assertNull(converted);
    }

    @Test
    void shouldHandleNullBallotJsonToDomainConversion() {
        Ballot converted = ballotConverter.toDomain(null);
        Assertions.assertNull(converted);
    }
}
