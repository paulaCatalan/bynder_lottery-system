package com.pcr.lottery_system.infrastructure.converter;

import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.infrastructure.dto.LotteryEventJson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;

class LotteryEventConverterTest {

    LotteryEventConverter lotteryEventConverter = new LotteryEventConverter();

    @Test
    void shouldConvertLotteryEventJsonToLotteryEvent() {
        String idString = UUID.randomUUID().toString();
        String startTimeString = "2025-06-27T09:00:00Z";
        String endTimeString = "2025-06-27T23:59:59Z";
        LotteryStatus status = LotteryStatus.OPEN;
        String winnerBallotIdString = null;

        LotteryEventJson lotteryEventJson = new LotteryEventJson(
                idString,
                startTimeString,
                endTimeString,
                status,
                winnerBallotIdString
        );

        LotteryEvent expectedLotteryEvent = new LotteryEvent(
                idString,
                Instant.parse(startTimeString),
                Instant.parse(endTimeString),
                status,
                null
        );

        LotteryEvent convertedLotteryEvent = lotteryEventConverter.toDomain(lotteryEventJson);

        Assertions.assertEquals(expectedLotteryEvent, convertedLotteryEvent);
    }

    @Test
    void shouldConvertLotteryEventToLotteryEventJson() {
        String id = UUID.randomUUID().toString();
        Instant startTime = Instant.parse("2025-06-27T09:00:00Z");
        Instant endTime = Instant.parse("2025-06-27T23:59:59Z");
        LotteryStatus status = LotteryStatus.DRAWN;
        String winnerBallotId = UUID.randomUUID().toString();

        LotteryEvent lotteryEvent = new LotteryEvent(
                id,
                startTime,
                endTime,
                status,
                winnerBallotId
        );

        LotteryEventJson expectedLotteryEventJson = new LotteryEventJson(
                id.toString(),
                startTime.toString(),
                endTime.toString(),
                status,
                winnerBallotId.toString()
        );

        LotteryEventJson convertedLotteryEventJson = lotteryEventConverter.toDto(lotteryEvent);

        Assertions.assertEquals(expectedLotteryEventJson, convertedLotteryEventJson);

    }

    @Test
    void shouldHandleNullLotteryEventToDtoConversion() {
        // When
        LotteryEventJson converted = lotteryEventConverter.toDto(null);
        // Then
        Assertions.assertNull(converted);
    }

    @Test
    void shouldHandleNullLotteryEventJsonToDomainConversion() {
        LotteryEvent converted = lotteryEventConverter.toDomain(null);
        Assertions.assertNull(converted);
    }

    @Test
    void shouldConvertLotteryEventJsonToLotteryEventWithNullWinnerBallotId() {
        String idString = UUID.randomUUID().toString();
        String startTimeString = "2025-06-27T09:00:00Z";
        String endTimeString = "2025-06-27T23:59:59Z";
        LotteryStatus status = LotteryStatus.OPEN; // OPEN status should have null winnerBallotId

        LotteryEventJson lotteryEventJson = new LotteryEventJson(
                idString,
                startTimeString,
                endTimeString,
                status,
                null
        );

        LotteryEvent expectedLotteryEvent = new LotteryEvent(
                idString,
                Instant.parse(startTimeString),
                Instant.parse(endTimeString),
                status,
                null
        );

        LotteryEvent convertedLotteryEvent = lotteryEventConverter.toDomain(lotteryEventJson);

        Assertions.assertNotNull(convertedLotteryEvent);
        Assertions.assertEquals(expectedLotteryEvent, convertedLotteryEvent); // Records have good equals/hashCode
        Assertions.assertNull(convertedLotteryEvent.winnerBallotId());
    }

    @Test
    void shouldConvertLotteryEventToLotteryEventJsonWithNullWinnerBallotId() {
        String id = UUID.randomUUID().toString();
        Instant startTime = Instant.parse("2025-06-27T09:00:00Z");
        Instant endTime = Instant.parse("2025-06-27T23:59:59Z");
        LotteryStatus status = LotteryStatus.OPEN;

        LotteryEvent lotteryEvent = new LotteryEvent(
                id,
                startTime,
                endTime,
                status,
                null
        );

        LotteryEventJson expectedLotteryEventJson = new LotteryEventJson(
                id,
                startTime.toString(),
                endTime.toString(),
                status,
                null
        );

        // When
        LotteryEventJson convertedLotteryEventJson = lotteryEventConverter.toDto(lotteryEvent);

        // Then
        Assertions.assertNotNull(convertedLotteryEventJson);
        Assertions.assertEquals(expectedLotteryEventJson, convertedLotteryEventJson);
        Assertions.assertNull(convertedLotteryEventJson.winnerBallotId());
    }

    @Test
    void shouldThrowExceptionForInvalidInstantFormat() {
        LotteryEventJson lotteryEventJson = new LotteryEventJson(
                UUID.randomUUID().toString(),
                "invalid-date-format",
                "2025-06-27T23:59:59Z",
                LotteryStatus.OPEN,
                null
        );

        Assertions.assertThrows(DateTimeParseException.class, () -> {
            lotteryEventConverter.toDomain(lotteryEventJson);
        });
    }
}
