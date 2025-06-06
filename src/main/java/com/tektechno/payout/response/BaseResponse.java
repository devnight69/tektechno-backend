package com.tektechno.payout.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Base Response class.
 *
 * @author kousik manik
 */
@Service
public class BaseResponse {

  /**
   * Error Response with Exception argument.
   *
   * @param ex Exception
   * @return ResponseEntity
   */
  public ResponseEntity<?> errorResponse(Exception ex) {
    ResponseDto response = new ResponseDto();
    response.setResponse(false);
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    response.setMessage(ex.getMessage());
    response.setTimestamp(LocalDateTime.now());
    return ResponseEntity.status(500).body(response);
  }

  /**
   * Error Response with Throwable and messages.
   *
   * @param message String
   * @param ex      {@link Throwable}
   * @return ResponseEntity
   */

  public ResponseEntity<?> errorResponse(String message, Throwable ex) {
    ResponseDto response = new ResponseDto();
    response.setResponse(false);
    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    response.setMessage(message);
    response.setTimestamp(LocalDateTime.now());
    return ResponseEntity.status(500).body(response);
  }

  /**
   * Error Response with Throwable and status and message .
   *
   * @param status {@link HttpStatus}
   * @param data   {@link Object}
   * @return @{@link ResponseEntity}
   */
  public ResponseEntity<?> errorResponse(HttpStatus status, String message, Object data) {
    ResponseDto response = new ResponseDto();
    response.setResponse(false);
    response.setMessage(message);
    response.setStatus(status);
    response.setData(data);
    response.setTimestamp(LocalDateTime.now());
    return ResponseEntity.status(status).body(response);
  }

  /**
   * Error Response with Throwable and status.
   *
   * @param status {@link HttpStatus}
   * @param data   {@link Object}
   * @return @{@link ResponseEntity}
   */
  public ResponseEntity<?> errorResponse(HttpStatus status, Object data) {
    ResponseDto response = new ResponseDto();
    response.setResponse(false);
    response.setStatus(status);
    response.setData(data);
    response.setTimestamp(LocalDateTime.now());
    return ResponseEntity.status(status).body(response);
  }

  /**
   * Error Response with status and messages.
   *
   * @param status  @{@link HttpStatus}
   * @param message @{@link String}
   * @return ResponseEntity
   */
  public ResponseEntity<?> errorResponse(HttpStatus status, String message) {
    ResponseDto response = new ResponseDto();
    response.setResponse(false);
    response.setStatus(status);
    response.setMessage(message);
    response.setTimestamp(LocalDateTime.now());
    return ResponseEntity.status(status).body(response);
  }

  /**
   * Error Response with data.
   *
   * @param data @{@link Object}
   * @return ResponseEntity
   */
  public ResponseEntity<?> successResponse(Object data) {
    ResponseDto response = new ResponseDto();
    response.setResponse(true);
    response.setStatus(HttpStatus.ACCEPTED);
    response.setMessage("Success");
    response.setTimestamp(LocalDateTime.now());
    response.setData(data);
    return ResponseEntity.ok(response);
  }

  /**
   * Error Response with messages and data.
   *
   * @param message @{@link String}
   * @param data    @{@link Object}
   * @return ResponseEntity
   */
  public ResponseEntity<?> successResponse(String message, Object data) {
    ResponseDto response = new ResponseDto();
    response.setResponse(true);
    response.setStatus(HttpStatus.ACCEPTED);
    response.setMessage(message);
    response.setTimestamp(LocalDateTime.now());
    response.setData(data);
    return ResponseEntity.ok(response);
  }
}
