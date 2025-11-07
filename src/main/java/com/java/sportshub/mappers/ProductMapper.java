package com.java.sportshub.mappers;

import com.java.sportshub.dtos.ProductDTO;
import com.java.sportshub.models.Product;
import com.java.sportshub.dtos.CouponDTO;

import java.util.stream.Collectors;

public class ProductMapper {

  public static ProductDTO toDTO(Product product) {
    ProductDTO dto = new ProductDTO(product);
    return dto;
  }

  public static Product toEntity(ProductDTO dto) {
    Product product = new Product();
    product.setName(dto.getName());
    product.setDescription(dto.getDescription());
    product.setPrice(dto.getPrice());
    return product;
  }

  public static void updateEntity(Product product, ProductDTO dto) {
    if (dto.getName() != null) {
      product.setName(dto.getName());
    }
    if (dto.getDescription() != null) {
      product.setDescription(dto.getDescription());
    }
    if (dto.getPrice() > 0) {
      product.setPrice(dto.getPrice());
    }
  }
}
