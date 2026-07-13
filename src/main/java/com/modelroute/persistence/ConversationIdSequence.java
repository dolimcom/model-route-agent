package com.modelroute.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Database-backed sequence used to keep externally visible conversation IDs unique across restarts.
 */
@Entity
@Table(name = "conversation_id_sequence")
public class ConversationIdSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sequence_id")
    private Long sequenceId;

    public ConversationIdSequence() {
    }

    public Long getSequenceId() {
        return sequenceId;
    }
}
