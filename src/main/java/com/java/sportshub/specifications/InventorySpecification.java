package com.java.sportshub.specifications;

import com.java.sportshub.models.Inventory;
import org.springframework.data.jpa.domain.Specification;

public class InventorySpecification {

    // Especificación para filtrar por nombre (LIKE)
    public static Specification<Inventory> isOfType(String type) {
        return (root, query, cb) ->
                cb.equal(root.get("tipo"), type);
    }
}
