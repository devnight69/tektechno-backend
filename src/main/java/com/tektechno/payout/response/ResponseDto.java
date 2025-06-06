package com.tektechno.payout.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * this is a response dto .
 *
 * @author kousik manik
 */
@Data
public class ResponseDto {

  boolean response;
  String message;
  Object data;
  HttpStatus status;
  LocalDateTime timestamp;

  /**
   * this is a to json method .
   *
   * @return @{@link String}
   */
  public String toJson() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(this);
    } catch (Exception e) {
      return "{}"; // Handle serialization exception
    }
  }

}

