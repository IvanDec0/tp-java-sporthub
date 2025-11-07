package com.java.sportshub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.RoleDAO;
import com.java.sportshub.exceptions.AttributeExistsException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Role;

@Service
public class RoleService {

    @Autowired
    private RoleDAO roleDAO;

    public List<Role> getAllRoles() {
        return roleDAO.findAll();
    }

    public Role getRoleById(Long id) {
        return roleDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }

    @Transactional
    public Role createRole(Role role) {
        validateRole(role);

        if (roleDAO.existsByRoleName(role.getRoleName())) {
            throw new AttributeExistsException("Role", "roleName", role.getRoleName());
        }

        return roleDAO.save(role);
    }

    @Transactional
    public Role updateRole(Long id, Role roleDetails) {
        Role role = getRoleById(id);

        if (roleDetails.getRoleName() != null) {
            // Verificar si el nuevo nombre ya existe en otro rol
            if (roleDAO.existsByRoleName(roleDetails.getRoleName()) &&
                    !role.getRoleName().equals(roleDetails.getRoleName())) {
                throw new AttributeExistsException("Role", "roleName", roleDetails.getRoleName());
            }
            role.setRoleName(roleDetails.getRoleName());
        }

        return roleDAO.save(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = getRoleById(id);
        role.setIsActive(false);
        roleDAO.save(role);
    }

    @Transactional
    public Role activateRole(Long id) {
        Role role = roleDAO.findDeletedRoleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role (deleted)", "id", id));
        role.setIsActive(true);
        return roleDAO.save(role);
    }

    private void validateRole(Role role) {
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            throw new ValidationException("roleName", "Role name is required");
        }
    }
}
