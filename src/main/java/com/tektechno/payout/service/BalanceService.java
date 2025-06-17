package com.tektechno.payout.service;

import org.springframework.http.ResponseEntity;

public interface BalanceService {

  public ResponseEntity<?> getBalance();

}
