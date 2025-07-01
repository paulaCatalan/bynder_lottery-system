package com.pcr.lottery_system.infrastructure.persistence;

import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.infrastructure.JsonFileHandler;
import com.pcr.lottery_system.infrastructure.converter.ParticipantConverter;
import com.pcr.lottery_system.infrastructure.dto.ParticipantJson;
import com.pcr.lottery_system.infrastructure.dto.ParticipantListWrapper;
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
import static org.mockito.Mockito.*;


class JsonParticipantRepositoryTest {

    @Mock
    ParticipantConverter mockedConverter;

    @Mock
    JsonFileHandler<ParticipantJson, ParticipantListWrapper> mockedFileHandler;

    JsonParticipantRepository jsonParticipantRepository;

    private Path tempFilePath;

    private List<ParticipantJson> simulatedFileContent;


    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        tempFilePath = Files.createTempFile("test-participants", ".json");
        tempFilePath.toFile().deleteOnExit();

        jsonParticipantRepository = new JsonParticipantRepository(tempFilePath.toString(), mockedConverter, mockedFileHandler);

        simulatedFileContent = new ArrayList<>();

        when(mockedFileHandler.readFromFile(any(File.class), eq(ParticipantListWrapper.class)))
                .thenAnswer(invocation -> new ArrayList<>(simulatedFileContent));

        doNothing().when(mockedFileHandler).ensureFileExistsAndInitialized(any(File.class), eq(ParticipantListWrapper.class));

        doAnswer(invocation -> {
            List<ParticipantJson> dataList = invocation.getArgument(1);
            simulatedFileContent.clear();
            simulatedFileContent.addAll(dataList);
            return null;
        }).when(mockedFileHandler).writeToFile(any(File.class), any(List.class), eq(ParticipantListWrapper.class));
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
    void shouldReturnNullIfEmailIsNotPresentInJsonFile(){
        Participant participant = jsonParticipantRepository.findByEmail("notPresentEmail@email.com");
        Assertions.assertNull(participant);
    }

    @Test
    void shouldSaveParticipantToJson() throws IOException{
        String newParticipantId = UUID.randomUUID().toString();
        String newEmail = "test4@email.com";
        String newName = "Maria";
        Participant participantToSave = new Participant(newParticipantId, newEmail, newName);
        ParticipantJson participantJsonToSave = new ParticipantJson(newParticipantId, newEmail, newName);

        when(mockedConverter.toDto(participantToSave)).thenReturn(participantJsonToSave);
        when(mockedConverter.toDomain(participantJsonToSave)).thenReturn(participantToSave);

        jsonParticipantRepository.save(participantToSave);

        verify(mockedFileHandler).writeToFile(any(), any(), any());
    }

    @Test
    void shouldFindExistingParticipantByEmail() throws IOException {
        String existingId = "existing-id-123";
        String existingEmail = "existing@example.com";
        String existingName = "Existing User";
        ParticipantJson existingParticipantJson = new ParticipantJson(existingId, existingEmail, existingName);

        simulatedFileContent.add(existingParticipantJson);

        Participant expectedParticipant = new Participant(existingId, existingEmail, existingName);
        when(mockedConverter.toDomain(existingParticipantJson)).thenReturn(expectedParticipant);

        when(mockedFileHandler.readFromFile(any(File.class), eq(ParticipantListWrapper.class)))
                .thenReturn(simulatedFileContent);

        Participant foundParticipant = jsonParticipantRepository.findByEmail(existingEmail);

        Assertions.assertNotNull(foundParticipant);
        Assertions.assertEquals(expectedParticipant, foundParticipant);
    }

}
