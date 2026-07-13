package com.modelroute.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findAllByConversationConversationIdOrderByCreatedAtAscIdAsc(String conversationId);
}
