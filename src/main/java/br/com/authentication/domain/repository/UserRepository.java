package br.com.authentication.domain.repository;

import br.com.authentication.domain.model.ExternalProvider;
import br.com.authentication.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByPasswordResetToken(String token);

    Optional<User> findByExternalProviderIdAndExternalProvider(String externalProviderId,
                                                               ExternalProvider externalProvider);

    boolean existsByEmail(String email);
}
