package com.java.sportshub.exceptions;

public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String resource, String action) {
    super(String.format("Unauthorized to %s on resource '%s'", action, resource));
  }
}
