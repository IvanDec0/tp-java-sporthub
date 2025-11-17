package com.java.sportshub.controllers;

import java.util.List;
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

import com.java.sportshub.dtos.RoleDTO;
import com.java.sportshub.mappers.RoleMapper;
import com.java.sportshub.middlewares.RequiredRoles;
import com.java.sportshub.models.Role;
import com.java.sportshub.services.RoleService;

@RestController
@RequestMapping("/api/roles")
@RequiredRoles({ "ADMIN" })
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles()
                .stream()
                .map(RoleMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(RoleMapper.toDTO(role));
    }

    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
        Role role = RoleMapper.toEntity(roleDTO);
        Role createdRole = roleService.createRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(RoleMapper.toDTO(createdRole));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable Long id,
            @RequestBody RoleDTO roleDTO) {
        Role roleDetails = RoleMapper.toEntity(roleDTO);
        Role updatedRole = roleService.updateRole(id, roleDetails);
        return ResponseEntity.ok(RoleMapper.toDTO(updatedRole));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<RoleDTO> activateRole(@PathVariable Long id) {
        Role activatedRole = roleService.activateRole(id);
        return ResponseEntity.ok(RoleMapper.toDTO(activatedRole));
    }
}
