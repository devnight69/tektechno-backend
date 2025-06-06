package com.tektechno.payout.exceptions;


import com.tektechno.payout.response.BaseResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Global custom exception handler to manage various exceptions in the application.
 *
 * <p>This class provides centralized exception handling across all {@code @RequestMapping} methods
 * within {@code @Controller} or {@code @RestController}.
 *
 * <p>Each exception handler method logs the details of the exception and constructs an appropriate
 * response for the client.
 *
 * <p>Author: Kousik Manik
 */
@ControllerAdvice
public class CustomExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

  @Autowired
  private BaseResponse baseResponse;

  /**
   * Handles validation exceptions for method arguments.
   *
   * @param ex instance of {@link MethodArgumentNotValidException}.
   * @return ResponseEntity containing validation error details.
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
    // logger.error("Validation exception occurred: {}", ex.getMessage(), ex);
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = "message";
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "Validation Error.", errors);
  }

  /**
   * Handles method validation exceptions.
   *
   * @param ex instance of {@link HandlerMethodValidationException}.
   * @return ResponseEntity containing validation error details.
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<?> handleMethodValidationExceptions(HandlerMethodValidationException ex) {
    // logger.error("Method validation exception occurred: {}", ex.getMessage(), ex);
    Map<String, String> errors = new HashMap<>();

    if (!ex.getValueResults().isEmpty()) {
      ex.getValueResults().forEach(parameterValidationResult -> {
        parameterValidationResult.getResolvableErrors().forEach(messageSourceResolvable -> {
          String fieldName = "message";
          String errorMessage = messageSourceResolvable.getDefaultMessage();
          logger.debug("Validation error: {}", errorMessage);
          errors.put(fieldName, errorMessage);
        });
      });
    }
    return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "Validation Error.", errors);
  }

  /**
   * Handles constraint violations such as invalid request parameters.
   *
   * @param ex instance of {@link ConstraintViolationException}.
   * @return ResponseEntity containing validation error details.
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<?> handleConstraintViolationExceptions(ConstraintViolationException ex) {
    logger.error("Constraint violation exception occurred: {}", ex.getMessage(), ex);
    Map<String, String> errors = new HashMap<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      String fieldName = "message";
      String errorMessage = violation.getMessage();
      errors.put(fieldName, errorMessage);
    }
    return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "Validation Error.", errors);
  }

  /**
   * Handles missing request parameter exceptions.
   *
   * @param ex instance of {@link MissingServletRequestParameterException}.
   * @return ResponseEntity containing error details.
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler({MissingServletRequestParameterException.class})
  public ResponseEntity<?> handleMissingRequestParamExceptions(MissingServletRequestParameterException ex) {
    logger.error("Missing request parameter exception occurred: {}", ex.getMessage(), ex);
    Map<String, String> errors = new HashMap<>();
    String fieldName = "message";
    String errorMessage = ex.getParameterName() + " parameter is missing";
    errors.put(fieldName, errorMessage);
    return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "Validation Error.", errors);
  }

  /**
   * Handles HTTP request method not supported exceptions.
   *
   * @param ex instance of {@link HttpRequestMethodNotSupportedException}.
   * @param request instance of {@link WebRequest}.
   * @return ResponseEntity containing error details.
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                    WebRequest request) {
    logger.error("Request method not supported exception occurred: {}", ex.getMessage(), ex);
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.METHOD_NOT_ALLOWED.value());
    body.put("error", "Method Not Allowed");
    body.put("message", ex.getMessage());
    body.put("path", request.getDescription(false).replace("uri=", ""));

    return baseResponse.errorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Validation Error.", body);
  }

  /**
   * Handles all other uncaught exceptions.
   *
   * @param ex instance of {@link Exception}.
   * @param request instance of {@link WebRequest}.
   * @return ResponseEntity containing error details.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleAllExceptions(Exception ex, WebRequest request) {
    logger.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    body.put("error", "Internal Server Error");
    body.put("message", ex.getMessage());
    body.put("path", request.getDescription(false).replace("uri=", ""));

    return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, body);
  }
}