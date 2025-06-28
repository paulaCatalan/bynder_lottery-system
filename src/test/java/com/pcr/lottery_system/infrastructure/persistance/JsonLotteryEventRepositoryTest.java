package com.pcr.lottery_system.infrastructure.persistance;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
        LotteryEvent openLotteryEvent = new LotteryEvent(
                "id",
                Instant.now(),
                Instant.now().plus(15, ChronoUnit.HOURS),
                LotteryStatus.OPEN,
                null
        );
        simulatedFileContent.add(openLotteryEventJson);
        simulatedFileContent.add(closedLotteryEventJson);

        List<LotteryEvent> drawnLotteryEventsFound = jsonLotteryEventRepository.findLotteryEventByStatus(LotteryStatus.DRAWN);

        Assertions.assertEquals(List.of(), drawnLotteryEventsFound);
    }

}