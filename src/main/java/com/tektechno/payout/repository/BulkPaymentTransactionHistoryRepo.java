package com.tektechno.payout.repository;

import com.tektechno.payout.model.BulkPaymentTransactionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This is a bulk payment transaction history repository class.
 *
 * @author Kousik Manik
 */
@Repository
public interface BulkPaymentTransactionHistoryRepo extends JpaRepository<BulkPaymentTransactionHistory, Long>{

  List<BulkPaymentTransactionHistory> findByTransactionIdAndMemberIdOrderByCreatedAtDesc(
      String transactionId, String memberId);

}
