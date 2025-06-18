package com.tektechno.payout.repository;

import com.tektechno.payout.model.WalletBalance;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletBalanceRepository extends JpaRepository<WalletBalance, Long> {

  double findBalanceByMemberId(String memberId);

  @Modifying
  @Transactional
  @Query("UPDATE WalletBalance w SET w.balance = :balance, w.updatedAt = CURRENT_TIMESTAMP WHERE w.memberId = :memberId")
  int updateBalanceByMemberId(String memberId, double balance);

}
