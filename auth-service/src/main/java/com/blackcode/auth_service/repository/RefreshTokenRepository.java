package com.blackcode.auth_service.repository;

import com.blackcode.auth_service.model.RefreshToken;
import com.blackcode.auth_service.model.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userAuth.id = :userId")
    Optional<RefreshToken> findByUserAuthId(String userId);

    @Modifying
    void deleteByUserAuth(UserAuth userAuth);
}