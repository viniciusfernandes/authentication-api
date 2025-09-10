package br.com.ovigia.service;

import br.com.ovigia.domain.model.Token;
import br.com.ovigia.domain.model.TokenType;
import br.com.ovigia.domain.model.User;

public interface ITokenService {
    
    Token createToken(User user, TokenType type, int expiryHours);
    
    Token findByTokenAndType(String token, TokenType type);
    
    void markTokenAsUsed(String token, TokenType type);
    
    boolean isTokenValid(String token, TokenType type);
}
