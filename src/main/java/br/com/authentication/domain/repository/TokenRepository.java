package br.com.authentication.domain.repository;

import br.com.authentication.domain.model.Token;
import br.com.authentication.domain.model.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    
    Optional<Token> findByTokenAndType(String token, TokenType type);
    
    void deleteByUserAndType(br.com.authentication.domain.model.User user, TokenType type);
}
