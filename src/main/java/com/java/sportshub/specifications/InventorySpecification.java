package com.java.sportshub.specifications;

import com.java.sportshub.models.Inventory;
import com.java.sportshub.models.Product;
import com.java.sportshub.models.Store;
import org.springframework.data.jpa.domain.Specification;

public class InventorySpecification {

    // Especificación para filtrar por nombre (LIKE)
    public static Specification<Inventory> isOfType(String type) {
        return (root, query, cb) ->
                cb.equal(root.get("tipo"), type);
    }

    public static Specification<Inventory> belongsToProduct(Product product) {
        return (root, query, cb) ->
                cb.equal(root.get("product"), product);
    }

    public static Specification<Inventory> belongsToStore(Store store) {
        return (root, query, cb) ->
                cb.equal(root.get("store"), store);
    }
}
