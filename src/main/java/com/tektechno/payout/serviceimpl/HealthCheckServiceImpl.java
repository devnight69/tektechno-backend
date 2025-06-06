package com.tektechno.payout.serviceimpl;

import com.tektechno.payout.service.HealthCheckService;
import org.springframework.stereotype.Service;

/**
 * this is a health check service implementation class .
 *
 * @author kousik manik
 */
@Service
public class HealthCheckServiceImpl implements HealthCheckService {

  /**
   * this is a check application health method .
   *
   * @return @{@link String}
   */
  @Override
  public String checkApplicationHealth() {
    return "TEK_TECHNO_PAYOUT_APPLICATION_HEALTHY: OKAY";
  }
}
