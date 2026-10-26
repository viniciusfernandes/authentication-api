package br.com.authentication.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Definition of a claim that may be written into the JWT (e.g. "companyId").
 * The value of the claim for each user lives in {@link UserClaim}.
 */
@Entity
@Table(name = "claims")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    public Long id;

    @Column(nullable = false, unique = true, length = 100)
    public String name;

    public String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false)
    @Builder.Default
    public ClaimValueType valueType = ClaimValueType.STRING;
}
