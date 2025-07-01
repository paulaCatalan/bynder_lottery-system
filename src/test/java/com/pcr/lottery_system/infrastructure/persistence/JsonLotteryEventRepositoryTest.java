package com.pcr.lottery_system.infrastructure.persistence;

import com.pcr.lottery_system.domain.model.LotteryEvent;
import com.pcr.lottery_system.domain.model.LotteryStatus;
import com.pcr.lottery_system.infrastructure.JsonFileHandler;
import com.pcr.lottery_system.infrastructure.converter.LotteryEventConverter;
import com.pcr.lottery_system.infrastructure.dto.LotteryEventJson;
import com.pcr.lottery_system.infrastructure.dto.LotteryEventListWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JsonLotteryEventRepositoryTest {

    @Mock
    LotteryEventConverter mockedConverter;

    @Mock
    JsonFileHandler<LotteryEventJson, LotteryEventListWrapper> mockedFileHandler;

    JsonLotteryEventRepository jsonLotteryEventRepository;

    private Path tempFilePath;

    private List<LotteryEventJson> simulatedFileContent;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        tempFilePath = Files.createTempFile("test-participants", ".json");
        tempFilePath.toFile().deleteOnExit();

        jsonLotteryEventRepository = new JsonLotteryEventRepository(tempFilePath.toString(), mockedConverter, mockedFileHandler);

        simulatedFileContent = new ArrayList<>();

        when(mockedFileHandler.readFromFile(any(File.class), eq(LotteryEventListWrapper.class)))
                .thenAnswer(invocation -> new ArrayList<>(simulatedFileContent));

        doNothing().when(mockedFileHandler).ensureFileExistsAndInitialized(any(File.class), eq(LotteryEventListWrapper.class));

        doAnswer(invocation -> {
            List<LotteryEventJson> dataList = invocation.getArgument(1);
            simulatedFileContent.clear();
            simulatedFileContent.addAll(dataList);
            return null;
        }).when(mockedFileHandler).writeToFile(any(File.class), any(List.class), eq(LotteryEventListWrapper.class));
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(tempFilePath);
        } catch (IOException e) {
            System.err.println("Error deleting temporary test file: " + e.getMessage());
        }
    }

    @Test
    void shouldSaveLotteryEventToJsonFile() throws IOException {
        LotteryEvent lotteryEventToSave = LotteryEvent.createLottery(
                "id",
                Instant.now(),
                Instant.now().plus(15, ChronoUnit.HOURS)
        );
        LotteryEventJson lotteryEventJson = new LotteryEventJson(
                "id",
                Instant.now().toString(),
                Instant.now().plus(15, ChronoUnit.HOURS).toString(),
                LotteryStatus.OPEN,
                null
        );
        when(mockedConverter.toDto(lotteryEventToSave)).thenReturn(lotteryEventJson);
        when(mockedConverter.toDomain(lotteryEventJson)).thenReturn(lotteryEventToSave);

        jsonLotteryEventRepository.save(lotteryEventToSave);

        verify(mockedFileHandler).writeToFile(any(), any(), any());

    }

    @Test
    void shouldFindAndReturnLotteryEventByStatus() {
        LotteryEventJson openLotteryEventJson = new LotteryEventJson(
                "id",
                Instant.now().toString(),
                Instant.now().plus(15, ChronoUnit.HOURS).toString(),
                LotteryStatus.OPEN,
                null
        );
        LotteryEventJson closedLotteryEventJson = new LotteryEventJson(
                "id2",
                Instant.now().minus(15, ChronoUnit.HOURS).toString(),
                Instant.now().minus(5, ChronoUnit.HOURS).toString(),
                LotteryStatus.CLOSED,
                null
        );
        LotteryEvent openLotteryEvent = new LotteryEvent(
                "id",
                Instant.now(),
                Instant.now().plus(15, ChronoUnit.HOURS),
                LotteryStatus.OPEN,
                null
        );
        List<LotteryEvent> expectedOpenLotteryEventsList = List.of(openLotteryEvent);
        simulatedFileContent.add(openLotteryEventJson);
        simulatedFileContent.add(closedLotteryEventJson);
        when(mockedConverter.toDomain(openLotteryEventJson)).thenReturn(openLotteryEvent);


        List<LotteryEvent> openLotteryEventsFound = jsonLotteryEventRepository.findLotteryEventByStatus(LotteryStatus.OPEN);

        Assertions.assertEquals(expectedOpenLotteryEventsList, openLotteryEventsFound);
    }

    @Test
    void shouldFindAndReturnLotteryEventById() {
        LotteryEventJson lotteryEventJson1 = new LotteryEventJson(
                "id",
                Instant.now().toString(),
                Instant.now().plus(15, ChronoUnit.HOURS).toString(),
                LotteryStatus.OPEN,
                null
        );
        LotteryEventJson lotteryEventJson2 = new LotteryEventJson(
                "id2",
                Instant.now().minus(15, ChronoUnit.HOURS).toString(),
                Instant.now().minus(5, ChronoUnit.HOURS).toString(),
                LotteryStatus.CLOSED,
                null
        );
        LotteryEvent lotteryEvent1 = new LotteryEvent(
                "id",
                Instant.now(),
                Instant.now().plus(15, ChronoUnit.HOURS),
                LotteryStatus.OPEN,
                null
        );

        simulatedFileContent.add(lotteryEventJson1);
        simulatedFileContent.add(lotteryEventJson2);
        when(mockedConverter.toDomain(lotteryEventJson1)).thenReturn(lotteryEvent1);


        LotteryEvent lotteryEventFound = jsonLotteryEventRepository.findLotteryEventById("id");

        Assertions.assertEquals(lotteryEvent1, lotteryEventFound);
    }

    @Test
    void shouldFindLotteryEventsByEndDate() {
        LocalDate testDate = LocalDate.now();
        Instant endOfDay = testDate.atTime(23, 59, 59).atOffset(ZoneOffset.UTC).toInstant();
        String id1 = UUID.randomUUID().toString();
        LotteryEventJson eventJson1 = new LotteryEventJson(
                id1,
                testDate.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant().toString(),
                endOfDay.toString(),
                LotteryStatus.DRAWN,
                UUID.randomUUID().toString()
        );
        LotteryEvent domainEvent1 = new LotteryEvent(
                id1,
                Instant.parse(eventJson1.startTime()),
                Instant.parse(eventJson1.endTime()),
                LotteryStatus.DRAWN,
                eventJson1.winnerBallotId()
        );

        String id2 = UUID.randomUUID().toString();
        LotteryEventJson eventJson2 = new LotteryEventJson(
                id2,
                testDate.minusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).toInstant().toString(),
                testDate.minusDays(1).atTime(23, 59, 59).atOffset(ZoneOffset.UTC).toInstant().toString(), // Ends on previous day
                LotteryStatus.CLOSED,
                null
        );
        LotteryEvent domainEvent2 = new LotteryEvent(
                id2,
                Instant.parse(eventJson2.startTime()),
                Instant.parse(eventJson2.endTime()),
                LotteryStatus.CLOSED,
                null
        );

        String id3 = UUID.randomUUID().toString();
        LotteryEventJson eventJson3 = new LotteryEventJson(
                id3,
                testDate.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant().toString(),
                endOfDay.toString(),
                LotteryStatus.OPEN,
                null
        );
        LotteryEvent domainEvent3 = new LotteryEvent(
                id3,
                Instant.parse(eventJson3.startTime()),
                Instant.parse(eventJson3.endTime()),
                LotteryStatus.OPEN,
                null
        );


        simulatedFileContent.add(eventJson1);
        simulatedFileContent.add(eventJson2);
        simulatedFileContent.add(eventJson3);

        when(mockedConverter.toDomain(eventJson1)).thenReturn(domainEvent1);
        when(mockedConverter.toDomain(eventJson2)).thenReturn(domainEvent2);
        when(mockedConverter.toDomain(eventJson3)).thenReturn(domainEvent3);

        List<LotteryEvent> expectedEvents = Arrays.asList(domainEvent1, domainEvent3);

        List<LotteryEvent> foundEvents = jsonLotteryEventRepository.findLotteryEventByEndLotteryDate(testDate);

        Assertions.assertNotNull(foundEvents);
        Assertions.assertEquals(expectedEvents.size(), foundEvents.size());
        Assertions.assertTrue(foundEvents.containsAll(expectedEvents));
        Assertions.assertTrue(expectedEvents.containsAll(foundEvents));
    }

    @Test
    void shouldReturnEmptyListIfNoLotteryEventsOfDesiredStatus() {
        LotteryEventJson openLotteryEventJson = new LotteryEventJson(
                "id",
                Instant.now().toString(),
                Instant.now().plus(15, ChronoUnit.HOURS).toString(),
                LotteryStatus.OPEN,
                null
        );
        LotteryEventJson closedLotteryEventJson = new LotteryEventJson(
                "id2",
                Instant.now().minus(15, ChronoUnit.HOURS).toString(),
                Instant.now().minus(5, ChronoUnit.HOURS).toString(),
                LotteryStatus.CLOSED,
                null
        );
        simulatedFileContent.add(openLotteryEventJson);
        simulatedFileContent.add(closedLotteryEventJson);

        List<LotteryEvent> drawnLotteryEventsFound = jsonLotteryEventRepository.findLotteryEventByStatus(LotteryStatus.DRAWN);

        Assertions.assertEquals(List.of(), drawnLotteryEventsFound);
    }

    @Test
    void shouldUpdateLotteryEventInJsonFile() {
        LotteryEventJson openLotteryEventJson = new LotteryEventJson(
                "id",
                Instant.now().toString(),
                Instant.now().plus(15, ChronoUnit.HOURS).toString(),
                LotteryStatus.OPEN,
                null
        );
        LotteryEvent updatedLotteryEvent = new LotteryEvent(
                "id",
                Instant.now(),
                Instant.now().plus(15, ChronoUnit.HOURS),
                LotteryStatus.CLOSED,
                null
        );
        LotteryEventJson updatedLotteryEventJson = new LotteryEventJson(
                "id",
                Instant.now().toString(),
                Instant.now().plus(15, ChronoUnit.HOURS).toString(),
                LotteryStatus.CLOSED,
                null
        );
        simulatedFileContent.add(openLotteryEventJson);
        when(mockedConverter.toDto(updatedLotteryEvent)).thenReturn(updatedLotteryEventJson);
        when(mockedConverter.toDomain(updatedLotteryEventJson)).thenReturn(updatedLotteryEvent);

        jsonLotteryEventRepository.save(updatedLotteryEvent);

        Assertions.assertEquals(1, simulatedFileContent.size());

        LotteryEventJson storedEvent = simulatedFileContent.get(0);
        Assertions.assertEquals(updatedLotteryEventJson, storedEvent);
    }

}