package com.pcr.lottery_system.domain.repository;

import com.pcr.lottery_system.domain.model.Participant;

public interface ParticipantRepository {

    Participant findByEmail(String email);
    void save(Participant participant);
}
