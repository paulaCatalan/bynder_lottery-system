package com.pcr.lottery_system.infrastructure.controller;

import com.pcr.lottery_system.application.ParticipantService;
import com.pcr.lottery_system.infrastructure.dto.ParticipantRegistrationRequest;
import com.pcr.lottery_system.infrastructure.dto.ParticipantRegistrationResponse;
import com.pcr.lottery_system.infrastructure.dto.RegisterParticipantCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.CREATED;


class ParticipantControllerTest {
    ParticipantService participantService = mock();
    ParticipantController participantController = new ParticipantController(participantService);

    @Test
    void shouldRegisterParticipantAndReturnOkStatus() {
        ParticipantRegistrationRequest request = new ParticipantRegistrationRequest("email", "Maria");

        ResponseEntity<ParticipantRegistrationResponse> response = participantController.registerParticipant(request);
        RegisterParticipantCommand command = new RegisterParticipantCommand("email", "Maria");
        // verify that service has been called
        verify(participantService).registerParticipant(command);

        Assertions.assertEquals(CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Participant registered successfully", response.getBody().message());


    }

}