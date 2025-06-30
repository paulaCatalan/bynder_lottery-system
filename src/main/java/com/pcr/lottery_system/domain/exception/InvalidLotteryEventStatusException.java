package com.pcr.lottery_system.domain.exception;

public class InvalidLotteryEventStatusException extends RuntimeException {
    public InvalidLotteryEventStatusException(String message) {
        super(message);
    }
}
