package com.auth.backend;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT refresh FROM user WHERE username = :username", nativeQuery = true)
    String findRefreshTokenByUsername(@Param("username") String username);

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
