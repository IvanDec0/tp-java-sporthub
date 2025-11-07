package com.java.sportshub.exceptions;

public class BusinessRuleException extends RuntimeException {

  public BusinessRuleException(String message) {
    super(message);
  }

  public BusinessRuleException(String rule, String violation) {
    super(String.format("Business rule '%s' violation: %s", rule, violation));
  }
}
