package com.modelroute.persistence;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findAllByOrderByUpdatedAtDesc();

    Optional<Conversation> findByConversationId(String conversationId);
}
