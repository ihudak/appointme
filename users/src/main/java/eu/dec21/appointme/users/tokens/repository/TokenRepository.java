package eu.dec21.appointme.users.tokens.repository;

import eu.dec21.appointme.users.tokens.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
}
