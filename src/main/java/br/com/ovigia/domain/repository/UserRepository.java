package br.com.ovigia.domain.repository;

import br.com.ovigia.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByPasswordResetToken(String token);
    
    Optional<User> findByExternalProviderIdAndExternalProvider(String externalProviderId, 
                                                              br.com.ovigia.domain.model.ExternalProvider externalProvider);
    
    boolean existsByEmail(String email);
}
