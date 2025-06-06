package com.tektechno.payout.exceptions;

/**
 * Customize exception class for SnehBharat.
 *
 * @author Kousik manik
 */
public class TechnoException extends Exception {
  private String message;

  public TechnoException() {
    super();
  }

  public TechnoException(String message) {
    super(message);
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }

  public void setMessage(String message) {
    this.message = message;
  }
}

