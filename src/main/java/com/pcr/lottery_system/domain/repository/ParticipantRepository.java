package com.pcr.lottery_system.domain.repository;

import com.pcr.lottery_system.domain.model.Participant;

public interface ParticipantRepository {

    Participant findByEmail(String email);
    Participant findById(String participantId);
    void save(Participant participant);

}
