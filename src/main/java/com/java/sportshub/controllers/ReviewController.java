package com.java.sportshub.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.sportshub.dtos.ReviewDTO;
import com.java.sportshub.mappers.ReviewMapper;
import com.java.sportshub.middlewares.AuthenticatedUser;
import com.java.sportshub.middlewares.RequiredRoles;
import com.java.sportshub.models.Review;
import com.java.sportshub.models.User;
import com.java.sportshub.services.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

  @Autowired
  private ReviewService reviewService;

  @GetMapping
  public ResponseEntity<List<ReviewDTO>> getAllReviews() {
    List<Review> reviews = reviewService.getAllReviews();
    List<ReviewDTO> reviewDTOs = reviews.stream()
        .map(ReviewMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(reviewDTOs);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReviewDTO> getReviewById(@PathVariable("id") Long id) {
    Review review = reviewService.getReviewById(id);
    return ResponseEntity.ok(ReviewMapper.toDTO(review));
  }

  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ReviewDTO>> getReviewsByProductId(@PathVariable("productId") Long productId) {
    List<Review> reviews = reviewService.getReviewsByProductId(productId);
    List<ReviewDTO> reviewDTOs = reviews.stream()
        .map(ReviewMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(reviewDTOs);
  }

  @GetMapping("/store/{storeId}")
  public ResponseEntity<List<ReviewDTO>> getReviewsByStoreId(@PathVariable("storeId") Long storeId) {
    List<Review> reviews = reviewService.getReviewsByStoreId(storeId);
    List<ReviewDTO> reviewDTOs = reviews.stream()
        .map(ReviewMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(reviewDTOs);
  }

  @GetMapping("/product/{productId}/average-rating")
  public ResponseEntity<Map<String, Double>> getAverageRatingByProductId(@PathVariable("productId") Long productId) {
    Double averageRating = reviewService.getAverageRatingByProductId(productId);
    Map<String, Double> response = new HashMap<>();
    response.put("averageRating", averageRating);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/store/{storeId}/average-rating")
  public ResponseEntity<Map<String, Double>> getAverageRatingByStoreId(@PathVariable("storeId") Long storeId) {
    Map<String, Double> response = new HashMap<>();
    Double averageRating = reviewService.getAverageRatingByStoreId(storeId);
    response.put("averageRating", averageRating);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  @RequiredRoles({ "USER", "ADMIN" })
  public ResponseEntity<ReviewDTO> createReview(@AuthenticatedUser User authenticatedUser,
      @RequestBody ReviewDTO reviewDTO) {
    Review review = ReviewMapper.toEntity(reviewDTO);
    Review createdReview = reviewService.createReview(review, authenticatedUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(ReviewMapper.toDTO(createdReview));
  }

  @PutMapping("/{id}")
  @RequiredRoles({ "USER", "ADMIN" })
  public ResponseEntity<ReviewDTO> updateReview(
      @PathVariable("id") Long id,
      @AuthenticatedUser User authenticatedUser,
      @RequestBody ReviewDTO reviewDTO) {
    Review review = ReviewMapper.toEntity(reviewDTO);
    Review updatedReview = reviewService.updateReview(id, review, authenticatedUser);
    return ResponseEntity.ok(ReviewMapper.toDTO(updatedReview));
  }

  @DeleteMapping("/{id}")
  @RequiredRoles({ "USER", "ADMIN" })
  public ResponseEntity<ReviewDTO> deleteReview(@PathVariable("id") Long id,
      @AuthenticatedUser User authenticatedUser) {
    Review review = reviewService.deleteReview(id, authenticatedUser);
    return ResponseEntity.ok(ReviewMapper.toDTO(review));
  }
}
