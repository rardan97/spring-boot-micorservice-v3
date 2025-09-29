package com.blackcode.auth_service.repository;

import com.blackcode.auth_service.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, String> {

    Optional<UserAuth> findByUsername(String username);

    Boolean existsByUsername(String username);

}
