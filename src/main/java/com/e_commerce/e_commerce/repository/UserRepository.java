package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.enums.Role;
import com.e_commerce.e_commerce.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByEnabledTrue(Pageable pageable);

    Page<User> findByRoleAndEnabledTrue(Role role, Pageable pageable);
}
