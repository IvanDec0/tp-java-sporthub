package com.java.sportshub.mappers;

import com.java.sportshub.dtos.ReviewDTO;
import com.java.sportshub.models.Product;
import com.java.sportshub.models.Review;
import com.java.sportshub.models.Store;

public class ReviewMapper {

  public static ReviewDTO toDTO(Review review) {
    return new ReviewDTO(review);
  }

  public static Review toEntity(ReviewDTO dto) {
    Review review = new Review();
    review.setRating(dto.getRating());
    review.setComment(dto.getComment());
    
    // Mapear Product si se proporciona productId
    // Nota: Solo establecemos el ID aquí, el servicio se encargará de cargar la entidad completa
    if (dto.getProductId() != null) {
      Product product = new Product();
      product.setId(dto.getProductId());
      review.setProduct(product);
    }
    
    // Mapear Store si se proporciona storeId
    // Nota: Solo establecemos el ID aquí, el servicio se encargará de cargar la entidad completa
    if (dto.getStoreId() != null) {
      Store store = new Store();
      store.setId(dto.getStoreId());
      review.setStore(store);
    }
    
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
