package com.modelroute.service;

import com.modelroute.persistence.ConversationIdSequence;
import com.modelroute.persistence.ConversationIdSequenceRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ConversationIdGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");

    private final ConversationIdSequenceRepository sequenceRepository;

    public ConversationIdGenerator(ConversationIdSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    public String nextId() {
        ConversationIdSequence sequence = sequenceRepository.saveAndFlush(new ConversationIdSequence());
        return DATE_TIME_FORMAT.format(LocalDateTime.now())
                + "-"
                + String.format(Locale.ROOT, "%010d", sequence.getSequenceId());
    }
}
