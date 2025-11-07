package com.java.sportshub.daos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.java.sportshub.models.Role;

@Repository
public interface RoleDAO extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r WHERE r.id = ?1 AND r.isActive = true")
    @NonNull
    Optional<Role> findById(@NonNull Long id);

    @Query("SELECT r FROM Role r WHERE r.id = ?1 AND r.isActive = false")
    @NonNull
    Optional<Role> findDeletedRoleById(@NonNull Long id);

    @Query("SELECT r FROM Role r WHERE r.roleName = ?1 AND r.isActive = true")
    @NonNull
    Optional<Role> findByRoleName(@NonNull String roleName);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.roleName = ?1 AND r.isActive = true")
    boolean existsByRoleName(String roleName);
}
