package com.pcr.lottery_system.infrastructure.persistence;

import com.pcr.lottery_system.domain.model.Ballot;
import com.pcr.lottery_system.domain.exception.BallotAlreadyExistsException;
import com.pcr.lottery_system.infrastructure.JsonFileHandler;
import com.pcr.lottery_system.infrastructure.converter.BallotConverter;
import com.pcr.lottery_system.infrastructure.dto.BallotJson;
import com.pcr.lottery_system.infrastructure.dto.BallotListWrapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class JsonBallotRepositoryTest {

    @Mock
    BallotConverter mockedConverter;

    @Mock
    JsonFileHandler<BallotJson, BallotListWrapper> mockedFileHandler;

    JsonBallotRepository jsonBallotRepository;

    private Path tempFilePath;
    private List<BallotJson> simulatedFileContent;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        tempFilePath = Files.createTempFile("test-ballots", ".json");
        tempFilePath.toFile().deleteOnExit();

        jsonBallotRepository = new JsonBallotRepository(tempFilePath.toString(), mockedConverter, mockedFileHandler);

        simulatedFileContent = new ArrayList<>();

        when(mockedFileHandler.readFromFile(any(File.class), eq(BallotListWrapper.class)))
                .thenAnswer(invocation -> new ArrayList<>(simulatedFileContent));

        doNothing().when(mockedFileHandler).ensureFileExistsAndInitialized(any(File.class), eq(BallotListWrapper.class));

        doAnswer(invocation -> {
            List<BallotJson> dataList = invocation.getArgument(1);
            simulatedFileContent.clear();
            simulatedFileContent.addAll(dataList);
            return null;
        }).when(mockedFileHandler).writeToFile(any(File.class), any(List.class), eq(BallotListWrapper.class));
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
    void shouldSaveNewBallotToJsonFile() {
        String ballotId = UUID.randomUUID().toString();
        String participantId = UUID.randomUUID().toString();
        String lotteryId = UUID.randomUUID().toString();

        Ballot newBallot = new Ballot(ballotId, participantId, lotteryId);
        BallotJson newBallotJson = new BallotJson(
                ballotId,
                participantId,
                lotteryId
        );

        when(mockedConverter.toDto(newBallot)).thenReturn(newBallotJson);

        jsonBallotRepository.save(newBallot);

        Assertions.assertEquals(1, simulatedFileContent.size());
        Assertions.assertEquals(newBallotJson, simulatedFileContent.get(0));
    }

    @Test
    void shouldThrowBallotAlreadyExistsExceptionWhenSavingExistingBallot() {
        String commonBallotId = UUID.randomUUID().toString();
        String commonParticipantId = UUID.randomUUID().toString();
        String commonLotteryId = UUID.randomUUID().toString();

        BallotJson originalBallotJson = new BallotJson(
                commonBallotId,
                commonParticipantId,
                commonLotteryId
                );
        simulatedFileContent.add(originalBallotJson);

        Ballot ballotToSaveAgain = new Ballot(
                commonBallotId,
                commonParticipantId,
                commonLotteryId
        );
        BallotJson ballotJsonToSaveAgain = new BallotJson(
                commonBallotId,
                commonParticipantId,
                commonLotteryId
        );

        when(mockedConverter.toDto(ballotToSaveAgain)).thenReturn(ballotJsonToSaveAgain);

        Assertions.assertThrows(BallotAlreadyExistsException.class, () -> jsonBallotRepository.save(ballotToSaveAgain));

        Assertions.assertEquals(1, simulatedFileContent.size());
        Assertions.assertEquals(originalBallotJson, simulatedFileContent.get(0));
    }
}
