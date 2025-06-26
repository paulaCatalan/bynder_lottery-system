package com.pcr.lottery_system.infrastructure.persistance;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import com.pcr.lottery_system.infrastructure.converter.ParticipantConverter;
import com.pcr.lottery_system.infrastructure.dto.ParticipantJson;
import com.pcr.lottery_system.infrastructure.dto.ParticipantsWrapper;
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
public class JsonParticipantRepository implements ParticipantRepository {
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
                writeParticipantsToFile(Collections.emptyList());
            } catch (IOException e) {
                System.err.println("Error initializing participants JSON file: " + e.getMessage());
                throw new RuntimeException("Failed to initialize JSON repository", e);
            }
        }

    }

    @Override
    public Participant findByEmail(String email) {
        try {
            List<ParticipantJson> allParticipants = readAllParticipantsFromFile(participantsFile.getPath());
            for (ParticipantJson participantJson : allParticipants) {
                if (participantJson.email().equals(email)) {
                    return new Participant(participantJson.participant_id(), participantJson.email(), participantJson.name());
//                    return participantConverter.toDomain(participantJson);
                }
            }
            return null; // Not found
        } catch (IOException e) {
            System.err.println("Error finding participant by email: " + email + " - " + e.getMessage());
            throw new RuntimeException("Persistence error finding participant by email", e);
        }
    }

    @Override
    public void save(Participant participant) {
        lock.writeLock().lock();
        try {
            List<ParticipantJson> currentParticipants = new ArrayList<>(readAllParticipantsFromFile(participantsFile.getPath()));
            ParticipantJson participantToSave = new ParticipantJson( //TODO: Extract to converter
                    participant.participantId(),
                    participant.email(),
                    participant.name());
            currentParticipants.add(participantToSave);

            writeParticipantsToFile(currentParticipants);

        } catch (IOException e) {
            System.err.println("Error saving participant: " + participant.participantId() + " - " + e.getMessage());
            throw new RuntimeException("Persistence error saving participant", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void writeParticipantsToFile(List<ParticipantJson> participantsJson) throws IOException {
        lock.writeLock().lock();
        try {
            ParticipantListWrapper wrapper = new ParticipantListWrapper(participantsJson);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(participantsFile, wrapper);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<ParticipantJson> readAllParticipantsFromFile(String filePath) throws IOException {
        lock.readLock().lock();
        List<ParticipantJson> participants = null;
        try {
            if (!participantsFile.exists() || participantsFile.length() == 0) {
                return Collections.emptyList();
            }

            try {
                ObjectMapper mapper = new ObjectMapper();

                File jsonFile = new File(filePath);

                ParticipantsWrapper wrapper = mapper.readValue(jsonFile, ParticipantsWrapper.class);

                participants = wrapper.getParticipants();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            lock.readLock().unlock();
        }
        return participants;
    }

}

record ParticipantListWrapper(
        @JsonProperty("participants") List<ParticipantJson> participants
) {
}


