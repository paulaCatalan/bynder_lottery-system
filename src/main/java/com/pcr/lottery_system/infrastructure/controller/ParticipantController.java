package com.pcr.lottery_system.infrastructure.controller;
import com.pcr.lottery_system.application.ParticipantService;
import com.pcr.lottery_system.infrastructure.dto.ParticipantRegistrationRequest;
import com.pcr.lottery_system.infrastructure.dto.ParticipantRegistrationResponse;
import com.pcr.lottery_system.infrastructure.dto.RegisterParticipantCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participant")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @PostMapping("/register")
    ResponseEntity<ParticipantRegistrationResponse> registerParticipant(@RequestBody ParticipantRegistrationRequest request) {
            try {
                RegisterParticipantCommand command = new RegisterParticipantCommand(request.email(), request.name());
                String participantId = participantService.registerParticipant(command);
                ParticipantRegistrationResponse response = new ParticipantRegistrationResponse(
                        participantId,
                        "Participant registered successfully"
                );
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } catch (com.pcr.lottery_system.domain.exception.DuplicateParticipantEmailException e) {
                ParticipantRegistrationResponse errorResponse = new ParticipantRegistrationResponse(
                        null,
                        e.getMessage()
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            } catch (Exception e) {
                System.err.println("An unexpected error occurred during participant registration: " + e.getMessage());
                ParticipantRegistrationResponse errorResponse = new ParticipantRegistrationResponse(
                        null,
                        "An unexpected error occurred during registration."
                );
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        }
    }
