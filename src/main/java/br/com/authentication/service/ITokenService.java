package br.com.authentication.service;

import br.com.authentication.domain.model.Token;
import br.com.authentication.domain.model.TokenType;
import br.com.authentication.domain.model.User;

public interface ITokenService {
    
    Token createToken(User user, TokenType type, int expiryHours);
    
    Token findByTokenAndType(String token, TokenType type);
    
    void markTokenAsUsed(String token, TokenType type);
    
    boolean isTokenValid(String token, TokenType type);
}
