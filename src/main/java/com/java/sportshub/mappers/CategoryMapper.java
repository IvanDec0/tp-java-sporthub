package com.java.sportshub.mappers;

import com.java.sportshub.dtos.CategoryDTO;
import com.java.sportshub.models.Category;

public class CategoryMapper {

  public static CategoryDTO toDTO(Category category) {
    return new CategoryDTO(category);
  }

  public static Category toEntity(CategoryDTO dto) {
    Category category = new Category();
    category.setName(dto.getName());
    return category;
  }

  public static void updateEntity(Category category, CategoryDTO dto) {
    if (dto.getName() != null) {
      category.setName(dto.getName());
    }
  }
}
