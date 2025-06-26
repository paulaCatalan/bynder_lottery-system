package com.pcr.lottery_system.application;

import com.pcr.lottery_system.domain.exception.DuplicateParticipantEmailException;
import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import com.pcr.lottery_system.infrastructure.dto.RegisterParticipantCommand;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    public ParticipantService(ParticipantRepository participantRepository) {
        this.participantRepository = participantRepository;
    }

    public String registerParticipant(RegisterParticipantCommand command) {
        if (participantRepository.findByEmail(command.email()) != null) {
            throw new DuplicateParticipantEmailException(command.email());

        }
        UUID newParticipantUuid = UUID.randomUUID();
        Participant newParticipant = new Participant(
                newParticipantUuid.toString(),
                command.email(),
                command.name()
        );
        participantRepository.save(newParticipant);
        return newParticipant.participantId();
    }
}
