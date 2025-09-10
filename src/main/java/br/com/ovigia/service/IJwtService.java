package br.com.ovigia.service;

import br.com.ovigia.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.function.Function;

public interface IJwtService {
    
    String extractUsername(String token);
    
    Date extractExpiration(String token);
    
    <T> T extractClaim(String token, Function<io.jsonwebtoken.Claims, T> claimsResolver);
    
    String generateToken(UserDetails userDetails);
    
    String generateToken(User user);
    
    Boolean validateToken(String token, UserDetails userDetails);
    
    Boolean validateToken(String token);
}
