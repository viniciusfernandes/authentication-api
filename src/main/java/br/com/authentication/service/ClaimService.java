package br.com.authentication.service;

import br.com.authentication.domain.exception.ConflictException;
import br.com.authentication.domain.model.Claim;
import br.com.authentication.domain.model.ClaimValueType;
import br.com.authentication.domain.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaimService implements IClaimService {

    private static final Pattern VALID_NAME = Pattern.compile("^[A-Za-z][A-Za-z0-9_.-]{0,99}$");

    // Names the token already uses (registered JWT claims and the ones always written by JwtService).
    private static final Set<String> RESERVED_NAMES = Set.of(
            "sub", "iss", "aud", "exp", "nbf", "iat", "jti", "userId", "email");

    private final ClaimRepository claimRepository;

    @Override
    public Claim create(String name, String description, ClaimValueType valueType) {
        String claimName = name.trim();

        if (!VALID_NAME.matcher(claimName).matches()) {
            throw new IllegalArgumentException(
                    "Claim name must start with a letter and contain only letters, digits, '_', '.' or '-' (max 100 characters)");
        }
        if (RESERVED_NAMES.contains(claimName)) {
            throw new IllegalArgumentException("Claim name '" + claimName + "' is reserved");
        }
        if (claimRepository.findByName(claimName).isPresent()) {
            throw new ConflictException("Claim " + claimName + " already exists");
        }

        Claim claim = Claim.builder()
                .name(claimName)
                .description(description)
                .valueType(valueType == null ? ClaimValueType.STRING : valueType)
                .build();

        return claimRepository.save(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Claim> findAll() {
        return claimRepository.findAll();
    }
}
