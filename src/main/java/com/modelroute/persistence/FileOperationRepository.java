package com.modelroute.persistence;

import com.modelroute.domain.FileOperationStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileOperationRepository extends JpaRepository<FileOperation, Long> {

    Optional<FileOperation> findByOperationId(String operationId);

    List<FileOperation> findTop50ByOrderByCreatedAtDesc();

    List<FileOperation> findTop50ByStatusOrderByCreatedAtDesc(FileOperationStatus status);

    Optional<FileOperation> findFirstByStatusOrderByExecutedAtDesc(FileOperationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select operation from FileOperation operation where operation.operationId = :operationId")
    Optional<FileOperation> findByOperationIdForUpdate(@Param("operationId") String operationId);
}
