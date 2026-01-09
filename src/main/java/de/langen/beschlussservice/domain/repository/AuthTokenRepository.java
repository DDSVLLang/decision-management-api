package de.langen.beschlussservice.domain.repository;

import de.langen.beschlussservice.domain.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AuthToken entity.
 *
 * @author Backend Team
 * @version 1.0
 */
@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    /**
     * Find token by token string.
     *
     * @param token token string
     * @return token if exists
     */
    Optional<AuthToken> findByToken(String token);

    /**
     * Find all valid (non-revoked, non-expired) tokens for a user.
     *
     * @param userId user UUID
     * @param now current time
     * @return list of valid tokens
     */
    @Query("SELECT t FROM AuthToken t WHERE t.user.id = :userId " +
            "AND t.revoked = false AND t.expiresAt > :now")
    List<AuthToken> findValidTokensByUserId(UUID userId, LocalDateTime now);

    /**
     * Find all tokens for a user.
     *
     * @param userId user UUID
     * @return list of tokens
     */
    List<AuthToken> findByUserId(UUID userId);

    /**
     * Delete expired tokens.
     *
     * @param now current time
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM AuthToken t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(LocalDateTime now);

    /**
     * Revoke all tokens for a user.
     *
     * @param userId user UUID
     * @return number of revoked tokens
     */
    @Modifying
    @Query("UPDATE AuthToken t SET t.revoked = true WHERE t.user.id = :userId")
    int revokeAllUserTokens(UUID userId);

    /**
     * Count valid tokens for a user.
     *
     * @param userId user UUID
     * @param now current time
     * @return count
     */
    @Query("SELECT COUNT(t) FROM AuthToken t WHERE t.user.id = :userId " +
            "AND t.revoked = false AND t.expiresAt > :now")
    long countValidTokensByUserId(UUID userId, LocalDateTime now);
}