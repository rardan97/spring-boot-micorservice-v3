package com.blackcode.auth_service.repository;

import com.blackcode.auth_service.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    Optional<TokenBlacklist> findByToken(String token);

    void deleteByExpiryDateBefore(LocalDateTime date);

}
