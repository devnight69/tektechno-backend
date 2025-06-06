package com.tektechno.payout.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * this is custom error response class for jwt related errors .
 *
 * @author kousik manik
 */
@Data
@AllArgsConstructor
public class CustomErrorResponse {
  private String errorCode;
  private String errorMessage;
}


