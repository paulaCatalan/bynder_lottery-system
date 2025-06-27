package com.pcr.lottery_system.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class DefaultJsonFileHandler<T, W> implements JsonFileHandler<T, W> {

    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public DefaultJsonFileHandler() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<T> readFromFile(File jsonFile, Class<W> wrapperClass) throws IOException {
        lock.readLock().lock();
        try {
            if (!jsonFile.exists() || jsonFile.length() == 0) {
                return Collections.emptyList();
            }
            try {

                W wrapper = objectMapper.readValue(jsonFile, wrapperClass);
                if (wrapper instanceof com.pcr.lottery_system.infrastructure.dto.ParticipantListWrapper) {
                    return (List<T>) ((com.pcr.lottery_system.infrastructure.dto.ParticipantListWrapper) wrapper).getParticipants();
                } else if (wrapper instanceof com.pcr.lottery_system.infrastructure.dto.LotteryEventListWrapper) {
                    return (List<T>) ((com.pcr.lottery_system.infrastructure.dto.LotteryEventListWrapper) wrapper).getLotteryEvents();
                  }
                throw new IllegalArgumentException("Unsupported wrapper type for readFromFile: " + wrapperClass.getName());

            } catch (MismatchedInputException e) {
                System.err.println("Warning: JSON file content does not match expected wrapper structure for " + jsonFile.getName() + ". Treating as empty. Error: " + e.getMessage());
                return Collections.emptyList();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void writeToFile(File jsonFile, List<T> dataList, Class<W> wrapperClass) throws IOException {
        lock.writeLock().lock();
        try {
            // Create an instance of the wrapper class and set its data list.
            W wrapper;
            try {
                if (wrapperClass == com.pcr.lottery_system.infrastructure.dto.ParticipantListWrapper.class) {
                    com.pcr.lottery_system.infrastructure.dto.ParticipantListWrapper participantsWrapper = new com.pcr.lottery_system.infrastructure.dto.ParticipantListWrapper();
                    participantsWrapper.setParticipants((List<com.pcr.lottery_system.infrastructure.dto.ParticipantJson>) dataList);
                    wrapper = (W) participantsWrapper;
                } else if (wrapperClass == com.pcr.lottery_system.infrastructure.dto.LotteryEventListWrapper.class) {
                    com.pcr.lottery_system.infrastructure.dto.LotteryEventListWrapper lotteryEventsWrapper = new com.pcr.lottery_system.infrastructure.dto.LotteryEventListWrapper();
                    lotteryEventsWrapper.setLotteryEvents((List<com.pcr.lottery_system.infrastructure.dto.LotteryEventJson>) dataList);
                    wrapper = (W) lotteryEventsWrapper;
                } else {
                    throw new IllegalArgumentException("Unsupported wrapper type for writeToFile: " + wrapperClass.getName());
                }
            } catch (Exception e) {
                throw new IOException("Failed to instantiate wrapper class or set data: " + wrapperClass.getName(), e);
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, wrapper);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void ensureFileExistsAndInitialized(File jsonFile, Class<W> wrapperClass) throws IOException {
        if (!jsonFile.exists()) {
            if (jsonFile.getParentFile() != null) {
                jsonFile.getParentFile().mkdirs();
            }
            jsonFile.createNewFile();
            writeToFile(jsonFile, Collections.emptyList(), wrapperClass);
        }
    }
}
