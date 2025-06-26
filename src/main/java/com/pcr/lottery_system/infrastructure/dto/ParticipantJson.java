package com.pcr.lottery_system.infrastructure.dto;

//I want to refactor this to not declare directly the same names as I have in the json
public record ParticipantJson(String participant_id, String email, String name) {
}
