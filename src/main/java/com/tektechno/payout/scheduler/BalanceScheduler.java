package com.tektechno.payout.scheduler;

import com.tektechno.payout.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This is a balance scheduler class.
 *
 * @author Kousik Manik
 */
@Component
public class BalanceScheduler {

  @Autowired
  private BalanceService balanceService;

  /**
   * Scheduled task to synchronize balance information every hour.
   * This method is triggered at the top of every hour (e.g., 12:00, 1:00, etc.)
   * using a cron expression.
   */
  @Scheduled(cron = "0 0 * * * *")
  public void syncBalance() {
    balanceService.getBalance();
  }

}
