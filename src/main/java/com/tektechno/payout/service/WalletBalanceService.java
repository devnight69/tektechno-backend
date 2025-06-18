package com.tektechno.payout.service;

import org.springframework.http.ResponseEntity;

public interface WalletBalanceService {

  public boolean updateWalletBalance(String memberId, double amount);

  public ResponseEntity<?> getWalletBalance(String memberId);

}
