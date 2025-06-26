package com.pcr.lottery_system.domain.exception;

public class DuplicateParticipantEmailException extends RuntimeException {
    public DuplicateParticipantEmailException(String email) {
        super("Participant with email '" + email + "' is already registered.");
    }
}
