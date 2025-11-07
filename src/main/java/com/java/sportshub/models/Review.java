package com.java.sportshub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Review extends Generic {

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne
  @JoinColumn(name = "store_id")
  private Store store;

  @Column(nullable = false)
  private Integer rating; // 1-5 estrellas

  @Column(columnDefinition = "TEXT")
  private String comment;

  @Column(name = "review_date")
  private LocalDateTime reviewDate;

  @PrePersist
  protected void onCreateReview() {
    this.reviewDate = LocalDateTime.now();
  }
}
