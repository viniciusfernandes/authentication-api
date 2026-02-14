package br.com.authentication.service;

import br.com.authentication.domain.model.Token;
import br.com.authentication.domain.model.TokenType;
import br.com.authentication.domain.model.User;
import br.com.authentication.domain.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService implements ITokenService {
    
    private final TokenRepository tokenRepository;
    
    public Token createToken(User user, TokenType type, int expiryHours) {
        // Delete any existing tokens of the same type for this user
        tokenRepository.deleteByUserAndType(user, type);
        
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expiryHours);
        
        Token token = Token.builder()
                .token(tokenValue)
                .type(type)
                .user(user)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        
        return tokenRepository.save(token);
    }
    
    @Transactional(readOnly = true)
    public Token findByTokenAndType(String token, TokenType type) {
        return tokenRepository.findByTokenAndType(token, type)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    }
    
    public void markTokenAsUsed(String token, TokenType type) {
        Token tokenEntity = findByTokenAndType(token, type);
        tokenEntity.markAsUsed();
        tokenRepository.save(tokenEntity);
    }
    
    public boolean isTokenValid(String token, TokenType type) {
        try {
            Token tokenEntity = findByTokenAndType(token, type);
            return tokenEntity.isValid();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
