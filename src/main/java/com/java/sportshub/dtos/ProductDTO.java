package com.java.sportshub.dtos;

import java.util.List;
import java.util.stream.Collectors;

import com.java.sportshub.models.Product;
import com.java.sportshub.dtos.CouponDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private List<CategoryDTO> categories;
    private List<CouponDTO> coupons;

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();

        if (product.getCategories() != null) {
            this.categories = product.getCategories().stream()
                    .map(CategoryDTO::new)
                    .collect(Collectors.toList());
        }

        if (product.getCoupons() != null) {
            this.coupons = product.getCoupons().stream()
                    .map(CouponDTO::new)
                    .collect(Collectors.toList());
        }
    }
}
