package com.tektechno.payout.controller;

import com.tektechno.payout.service.WalletBalanceService;
import com.tektechno.payout.utilities.DecodeJwtTokenUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet-balance")
public class WalletBalanceController {

  @Autowired
  private WalletBalanceService walletBalanceService;

  @Autowired
  private DecodeJwtTokenUtility decodeJwtTokenUtility;

  @GetMapping("/get")
  public ResponseEntity<?> getWalletBalance() {
    String memberId = decodeJwtTokenUtility.getMemberId();
    return walletBalanceService.getWalletBalance(memberId);
  }

}
