package com.pcr.lottery_system.domain.exception;

public class BallotAlreadyExistsException extends RuntimeException {
    public BallotAlreadyExistsException(String ballotId) {
        super("Ballot with ID '" + ballotId + "' already exists. Ballots cannot be updated.");
    }
}