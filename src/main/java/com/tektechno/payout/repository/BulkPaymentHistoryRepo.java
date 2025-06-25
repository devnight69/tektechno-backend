package com.tektechno.payout.repository;

import com.tektechno.payout.enums.BulkPaymentStatus;
import com.tektechno.payout.model.BulkPaymentHistory;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * This is a bulk payment repository class.
 *
 * @author Kousik Manik
 */
@Repository
public interface BulkPaymentHistoryRepo extends JpaRepository<BulkPaymentHistory, Long> {

  Page<BulkPaymentHistory> findByMemberIdOrderByCreatedAtDesc(String memberId, Pageable pageable);

  boolean existsByTransactionId(String transactionId);

  @Modifying
  @Transactional
  @Query("UPDATE BulkPaymentHistory b SET b.status = :status, b.updatedAt = CURRENT_TIMESTAMP " +
      "WHERE b.memberId = :memberId AND b.transactionId = :transactionId")
  int updateStatusByMemberIdAndTransactionId(@Param("status") BulkPaymentStatus status,
                                             @Param("memberId") String memberId,
                                             @Param("transactionId") String transactionId);

}
