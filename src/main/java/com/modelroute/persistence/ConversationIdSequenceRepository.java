package com.modelroute.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationIdSequenceRepository extends JpaRepository<ConversationIdSequence, Long> {
}
