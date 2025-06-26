package com.pcr.lottery_system.application;

import com.pcr.lottery_system.domain.exception.DuplicateParticipantEmailException;
import com.pcr.lottery_system.domain.model.Participant;
import com.pcr.lottery_system.domain.repository.ParticipantRepository;
import com.pcr.lottery_system.infrastructure.dto.RegisterParticipantCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParticipantServiceTest {

    ParticipantRepository participantRepository = mock(ParticipantRepository.class);
    ParticipantService participantService = new ParticipantService(participantRepository);

    @Test
    void shouldCallRepositoryToSaveParticipant() {
        RegisterParticipantCommand command = new RegisterParticipantCommand("email", "Maria");

        String participantId = participantService.registerParticipant(command);

        Participant participant = new Participant(participantId, "email", "Maria");

        verify(participantRepository).save(participant);

    }

    @Test
    void shouldNotSaveParticipantIfEmailIsAlreadyRegistered() {
        Participant alreadyRegisteredParticipant = new Participant("some-uuid-string", "existing@example.com", "Maria");
        RegisterParticipantCommand command = new RegisterParticipantCommand("existing@example.com", "Maria");

        when(participantRepository.findByEmail("existing@example.com"))
                .thenReturn(alreadyRegisteredParticipant);


        assertThrows(DuplicateParticipantEmailException.class, () -> participantService.registerParticipant(command));

        verify(participantRepository, never()).save(any(Participant.class));
    }

}