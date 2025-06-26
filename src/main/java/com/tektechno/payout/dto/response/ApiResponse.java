package com.tektechno.payout.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/**
 * This is an api response dto.
 *
 * @author Kousik Manik
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
  private String Status;
  private String SuccessMessage;
  private List<BalanceData> data;

  /**
   * This is a balance Data Dto.
   */
  @Data
  public static class BalanceData {
    private double balance;
  }

  }
