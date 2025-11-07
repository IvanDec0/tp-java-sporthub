package com.java.sportshub.exceptions;

public class InsufficientStockException extends RuntimeException {

  public InsufficientStockException(String message) {
    super(message);
  }

  public InsufficientStockException(String productName, Long requested, Long available) {
    super(String.format("Insufficient stock for '%s'. Requested: %d, Available: %d",
        productName, requested, available));
  }
}
