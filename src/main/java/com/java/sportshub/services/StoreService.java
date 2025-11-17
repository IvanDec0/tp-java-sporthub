package com.java.sportshub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.StoreDAO;
import com.java.sportshub.exceptions.AttributeExistsException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.UnauthorizedException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Store;
import com.java.sportshub.models.User;

@Service
public class StoreService {

    @Autowired
    private StoreDAO storeDAO;

    public List<Store> getAllStores() {
        return storeDAO.findAll();
    }

    public Store getStoreById(Long id) {
        return storeDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", id));
    }

    @Transactional
    public Store createStore(Store store, User owner) {
        if (owner == null) {
            throw new UnauthorizedException("Store", "create");
        }

        validateStore(store);

        if (storeDAO.existsByName(store.getName())) {
            throw new AttributeExistsException("Store", "name", store.getName());
        }

        if (store.getEmail() != null && storeDAO.existsByEmail(store.getEmail())) {
            throw new AttributeExistsException("Store", "email", store.getEmail());
        }

        store.setOwner(owner);

        return storeDAO.save(store);
    }

    @Transactional
    public Store updateStore(Long id, Store storeDetails, User owner) {
        Store store = getStoreById(id);

        ensureOwnership(store, owner, "update");

        if (storeDetails.getName() != null) {
            // Verificar si el nuevo nombre ya existe en otra tienda
            if (storeDAO.existsByName(storeDetails.getName()) &&
                    !store.getName().equals(storeDetails.getName())) {
                throw new AttributeExistsException("Store", "name", storeDetails.getName());
            }
            store.setName(storeDetails.getName());
        }

        if (storeDetails.getPhoneNumber() != null) {
            store.setPhoneNumber(storeDetails.getPhoneNumber());
        }

        if (storeDetails.getEmail() != null) {
            // Verificar si el nuevo email ya existe en otra tienda
            if (storeDAO.existsByEmail(storeDetails.getEmail()) &&
                    !store.getEmail().equals(storeDetails.getEmail())) {
                throw new AttributeExistsException("Store", "email", storeDetails.getEmail());
            }
            store.setEmail(storeDetails.getEmail());
        }

        if (storeDetails.getAddress() != null) {
            store.setAddress(storeDetails.getAddress());
        }

        return storeDAO.save(store);
    }

    @Transactional
    public void deleteStore(Long id, User owner) {
        Store store = getStoreById(id);
        ensureOwnership(store, owner, "delete");
        store.setIsActive(false);
        storeDAO.save(store);
    }

    private void validateStore(Store store) {
        if (store.getName() == null || store.getName().trim().isEmpty()) {
            throw new ValidationException("name", "Name is required");
        }
        if (store.getEmail() != null && !store.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("email", "Email format is invalid");
        }
    }

    private void ensureOwnership(Store store, User owner, String action) {
        if (owner == null || store.getOwner() == null || !store.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("Store", action);
        }
    }
}
