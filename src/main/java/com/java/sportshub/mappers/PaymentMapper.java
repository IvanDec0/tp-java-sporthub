package com.java.sportshub.mappers;

import com.java.sportshub.dtos.PaymentDTO;
import com.java.sportshub.models.Payment;

public class PaymentMapper {

  public static PaymentDTO toDTO(Payment payment) {
    return new PaymentDTO(payment);
  }

  public static Payment toEntity(PaymentDTO dto) {
    Payment payment = new Payment();
    payment.setAmount(dto.getAmount());
    payment.setPaymentMethod(dto.getPaymentMethod());
    payment.setPaymentStatus(dto.getPaymentStatus());
    payment.setTransactionId(dto.getTransactionId());
    payment.setPaymentDate(dto.getPaymentDate());
    payment.setNotes(dto.getNotes());
    return payment;
  }

  public static void updateEntity(Payment payment, PaymentDTO dto) {
    if (dto.getAmount() != null) {
      payment.setAmount(dto.getAmount());
    }
    if (dto.getPaymentMethod() != null) {
      payment.setPaymentMethod(dto.getPaymentMethod());
    }
    if (dto.getPaymentStatus() != null) {
      payment.setPaymentStatus(dto.getPaymentStatus());
    }
    if (dto.getNotes() != null) {
      payment.setNotes(dto.getNotes());
    }
  }
}
