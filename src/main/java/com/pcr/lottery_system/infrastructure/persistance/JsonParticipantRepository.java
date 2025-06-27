package com.pcr.lottery_system.infrastructure.persistance;// --- Infrastructure Layer: Repository Implementation (JSON File-Based) ---

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import com.pcr.lottery_system.infrastructure.dto.ParticipantJson;
import com.pcr.lottery_system.infrastructure.dto.ParticipantListWrapper;
import com.pcr.lottery_system.infrastructure.converter.ParticipantConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class JsonParticipantRepository implements ParticipantRepository{

    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final File participantsFile;

    public JsonParticipantRepository(
            @Value("${participants.file.path:participants.json}") String filePath
    ) {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);

        this.participantsFile = Paths.get(filePath).toFile();

        if (!participantsFile.exists()) {
            try {
                if (participantsFile.getParentFile() != null) {
                    participantsFile.getParentFile().mkdirs();
                }
                participantsFile.createNewFile();
                writeAllParticipantsToFile(Collections.emptyList());
            } catch (IOException e) {
                System.err.println("Error initializing participants JSON file: " + e.getMessage());
                throw new RuntimeException("Failed to initialize JSON repository", e);
            }
        }
    }

    private List<ParticipantJson> readAllParticipantsFromFile() throws IOException {
        lock.readLock().lock();
        try {
            if (!participantsFile.exists() || participantsFile.length() == 0) {
                return Collections.emptyList();
            }
            try {
                ParticipantListWrapper wrapper = objectMapper.readValue(participantsFile, ParticipantListWrapper.class);
                return wrapper.getParticipants() != null ? wrapper.getParticipants() : Collections.emptyList();
            } catch (MismatchedInputException e) {
                System.err.println("Warning: JSON file content does not match expected wrapper structure. Treating as empty. Error: " + e.getMessage());
                return Collections.emptyList();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private void writeAllParticipantsToFile(List<ParticipantJson> participantsJson) throws IOException {
        lock.writeLock().lock();
        try {
            ParticipantListWrapper wrapper = new ParticipantListWrapper();
            wrapper.setParticipants(participantsJson);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(participantsFile, wrapper);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Participant findByEmail(String email) {
        try {
            List<ParticipantJson> allParticipants = readAllParticipantsFromFile();
            for (ParticipantJson participantJson : allParticipants) {
                if (participantJson.email().equals(email)) {
                    return new Participant(participantJson.participant_id(), participantJson.email(), participantJson.name());
//                    return participantConverter.toDomain(participantJson);
                }
            }
            return null;
        } catch (IOException e) {
            System.err.println("Error finding participant by email: " + email + " - " + e.getMessage());
            throw new RuntimeException("Persistence error finding participant by email", e);
        }
    }

    @Override
    public void save(Participant participant) {
        lock.writeLock().lock();
        try {
            List<ParticipantJson> currentParticipants = new ArrayList<>(readAllParticipantsFromFile());
            ParticipantJson participantToSave = new ParticipantJson( //TODO: Extract to converter
                    participant.participantId(),
                    participant.email(),
                    participant.name());
            currentParticipants.add(participantToSave);

            writeAllParticipantsToFile(currentParticipants);
        } catch (IOException e) {
            System.err.println("Error saving participant: " + participant.participantId() + " - " + e.getMessage());
            throw new RuntimeException("Persistence error saving participant", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
