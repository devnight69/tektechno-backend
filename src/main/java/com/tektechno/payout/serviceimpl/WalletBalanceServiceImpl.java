package com.tektechno.payout.serviceimpl;

import com.tektechno.payout.repository.WalletBalanceRepository;
import com.tektechno.payout.response.BaseResponse;
import com.tektechno.payout.service.WalletBalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WalletBalanceServiceImpl implements WalletBalanceService {

  @Autowired
  private WalletBalanceRepository walletBalanceRepository;

  @Autowired
  private BaseResponse baseResponse;

  private static final Logger logger = LoggerFactory.getLogger(WalletBalanceServiceImpl.class);

  @Override
  public boolean updateWalletBalance(String memberId, double amount) {
    try {
      int updatedRows = walletBalanceRepository.updateBalanceByMemberId(memberId, amount);
      return updatedRows > 0;
    } catch (Exception e) {
      logger.error("Exception occurred while updating wallet balance for memberId: {}. Error: {}",
          memberId, e.getMessage(), e);
      return false;
    }
  }

  @Override
  public ResponseEntity<?> getWalletBalance(String memberId) {
    try {
      double balance = walletBalanceRepository.findBalanceByMemberId(memberId);
      return baseResponse.successResponse(balance);
    } catch (Exception e) {
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while fetching wallet balance for memberId: "
              + memberId + ". Please try again later.");
    }
  }
}
