package com.tektechno.payout.controller;

import com.tektechno.payout.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/balance")
public class BalanceController {

  @Autowired
  private BalanceService balanceService;

  @GetMapping("/get-balance")
  public ResponseEntity<?> getBalance() {
    return balanceService.getBalance();
  }

}
