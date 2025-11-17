package com.java.sportshub.services;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.java.sportshub.models.Cart;
import com.java.sportshub.models.Payment;
import com.java.sportshub.models.User;

@Service
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.from:no-reply@sportshub.local}")
  private String defaultFrom;

  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void sendEmail(String to, String subject, String body) {
    if (!StringUtils.hasText(to)) {
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(body);
    message.setFrom(defaultFrom);

    try {
      mailSender.send(message);
    } catch (MailException ex) {
      System.out.println("Error al enviar email: " + ex.getMessage());
    }
  }

  public void sendUserRegistrationEmail(User user) {
    if (user == null) {
      return;
    }

    String subject = "Bienvenido a SportHub!";
    String body = """
        Hola %s,

        Gracias por registrarte en SportHub. Ya podes iniciar sesion y alquilar tus equipos favoritos.

        Nos vemos pronto!

        Equipo SportHub
        """.formatted(resolveUserName(user));

    sendEmail(user.getEmail(), subject, body);
  }

  public void sendCartPurchaseEmail(Cart cart) {
    if (cart == null || cart.getUser() == null) {
      return;
    }

    String subject = "Confirmacion de compra - #" + cart.getId();
    String purchaseDate = cart.getUpdatedAt() != null
        ? cart.getUpdatedAt().toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault()))
        : "Reciente";

    String body = """
        Hola %s,

        Confirmamos que tu compra asociada al carrito #%d fue procesada con exito el %s.
        Importe total: %.2f

        Gracias por elegirnos!
        """.formatted(
        resolveUserName(cart.getUser()),
        cart.getId(),
        purchaseDate,
        cart.getTotalAmount() != null ? cart.getTotalAmount() : 0.0);

    sendEmail(cart.getUser().getEmail(), subject, body);
  }

  public void sendRefundConfirmationEmail(Payment payment) {
    if (payment == null || payment.getUser() == null) {
      return;
    }

    String subject = "Confirmacion de reembolso - #" + payment.getTransactionId();

    String body = """
        Hola %s,

        Confirmamos el reembolso por %.2f correspondiente a la transaccion %s.
        El monto sera acreditado por el mismo medio de pago utilizado.

        Ante cualquier duda, por favor contactanos.

        Equipo SportHub
        """.formatted(
        resolveUserName(payment.getUser()),
        payment.getAmount() != null ? payment.getAmount() : 0.0,
        payment.getTransactionId() != null ? payment.getTransactionId() : "-");

    sendEmail(payment.getUser().getEmail(), subject, body);
  }

  private String resolveUserName(User user) {
    if (user == null) {
      return "usuario";
    }

    if (StringUtils.hasText(user.getUserName())) {
      return user.getUserName();
    }

    if (StringUtils.hasText(user.getEmail())) {
      return user.getEmail();
    }

    return "usuario";
  }
}
