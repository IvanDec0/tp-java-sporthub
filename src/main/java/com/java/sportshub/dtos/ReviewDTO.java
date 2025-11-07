package com.java.sportshub.dtos;

import java.time.LocalDateTime;

import com.java.sportshub.models.Review;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewDTO {
  private Long id;
  private Integer rating;
  private String comment;
  private LocalDateTime reviewDate;
  private Long userId;
  private Long productId;
  private Long storeId;
  private UserDTO user;
  private ProductDTO product;
  private StoreDTO store;

  public ReviewDTO(Review review) {
    this.id = review.getId();
    this.userId = review.getUser() != null ? review.getUser().getId() : null;
    this.productId = review.getProduct() != null ? review.getProduct().getId() : null;
    this.storeId = review.getStore() != null ? review.getStore().getId() : null;
    this.user = review.getUser() != null ? new UserDTO(review.getUser()) : null;
    this.product = review.getProduct() != null ? new ProductDTO(review.getProduct()) : null;
    this.store = review.getStore() != null ? new StoreDTO(review.getStore()) : null;
    this.rating = review.getRating();
    this.comment = review.getComment();
    this.reviewDate = review.getReviewDate();
  }
}
