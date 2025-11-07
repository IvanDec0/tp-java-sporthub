package com.java.sportshub.exceptions;

public class ValidationException extends RuntimeException {

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String field, String constraint) {
    super(String.format("Validation failed for field '%s': %s", field, constraint));
  }
}
