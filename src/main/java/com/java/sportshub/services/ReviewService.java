package com.java.sportshub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.ReviewDAO;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.UnauthorizedException;
import com.java.sportshub.models.Review;
import com.java.sportshub.models.User;

@Service
public class ReviewService {

  @Autowired
  private ReviewDAO reviewDAO;

  public List<Review> getAllReviews() {
    return reviewDAO.findAll();
  }

  public Review getReviewById(Long id) {
    return reviewDAO.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
  }

  public List<Review> getReviewsByProductId(Long productId) {
    return reviewDAO.findActiveReviewsByProductId(productId);
  }

  public List<Review> getReviewsByStoreId(Long storeId) {
    return reviewDAO.findActiveReviewsByStoreId(storeId);
  }

  public Double getAverageRatingByProductId(Long productId) {
    Double avg = reviewDAO.getAverageRatingByProductId(productId);
    return avg != null ? avg : 0.0;
  }

  public Double getAverageRatingByStoreId(Long storeId) {
    Double avg = reviewDAO.getAverageRatingByStoreId(storeId);
    return avg != null ? avg : 0.0;
  }

  @Transactional
  public Review createReview(Review review, User user) {
    // TODO: Validar que el usuario haya realizado una compra/alquiler del producto
    // antes de permitir la reseña
    // TODO: Validar que el rating esté entre 1 y 5
    // TODO: Evitar múltiples reseñas del mismo usuario para el mismo producto

    if (review.getRating() < 1 || review.getRating() > 5) {
      throw new IllegalArgumentException("Rating must be between 1 and 5");
    }

    review.setUser(user);

    reviewDAO.save(review);
    return review;
  }

  @Transactional
  public Review updateReview(Long id, Review reviewDetails, User user) {
    Review review = getReviewById(id);

    ensureOwnership(review, user, "update");

    if (reviewDetails.getRating() != null) {
      if (reviewDetails.getRating() < 1 || reviewDetails.getRating() > 5) {
        throw new IllegalArgumentException("Rating must be between 1 and 5");
      }
      review.setRating(reviewDetails.getRating());
    }

    if (reviewDetails.getComment() != null) {
      review.setComment(reviewDetails.getComment());
    }

    reviewDAO.save(review);
    return review;
  }

  @Transactional
  public Review deleteReview(Long id, User user) {
    Review review = getReviewById(id);

    ensureOwnership(review, user, "delete");

    review.setIsActive(false);
    reviewDAO.save(review);
    return review;
  }

  private void ensureOwnership(Review review, User user, String action) {
    if (user == null || review.getUser() == null || !review.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedException("Review", action);
    }
  }
}
