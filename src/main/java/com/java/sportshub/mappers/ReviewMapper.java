package com.java.sportshub.mappers;

import com.java.sportshub.dtos.ReviewDTO;
import com.java.sportshub.models.Review;

public class ReviewMapper {

  public static ReviewDTO toDTO(Review review) {
    return new ReviewDTO(review);
  }

  public static Review toEntity(ReviewDTO dto) {
    Review review = new Review();
    review.setRating(dto.getRating());
    review.setComment(dto.getComment());
    return review;
  }

  public static void updateEntity(Review review, ReviewDTO dto) {
    if (dto.getRating() != null) {
      review.setRating(dto.getRating());
    }
    if (dto.getComment() != null) {
      review.setComment(dto.getComment());
    }
  }
}
