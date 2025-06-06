package com.tektechno.payout.repository;

import com.tektechno.payout.model.SendMoneyHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SendMoneyHistoryRepo extends JpaRepository<SendMoneyHistory, Long> {

  Page<SendMoneyHistory> findByBeneficiaryId(String beneficiaryId, Pageable pageable);

  Optional<SendMoneyHistory> findByOrderId(String orderId);

}
