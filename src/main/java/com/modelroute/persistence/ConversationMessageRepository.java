package com.modelroute.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findAllByConversationConversationIdOrderByCreatedAtAscIdAsc(String conversationId);

    Optional<ConversationMessage> findFirstByConversationConversationIdAndTaskTypeIsNotNullOrderByCreatedAtDescIdDesc(
            String conversationId);
}
