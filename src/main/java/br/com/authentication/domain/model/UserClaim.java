package br.com.authentication.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Association (N-N) between a {@link User} and a {@link Claim}, carrying the value of the claim for that user.
 */
@Entity
@Table(name = "user_claims")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserClaim {

    @EmbeddedId
    @EqualsAndHashCode.Include
    public UserClaimId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    public User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("claimId")
    @JoinColumn(name = "claim_id")
    public Claim claim;

    @Column(nullable = false)
    public String value;

    public UserClaim(User user, Claim claim, String value) {
        this.user = user;
        this.claim = claim;
        this.value = value;
        this.id = new UserClaimId(user.id, claim.id);
    }

    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserClaimId implements Serializable {

        @Column(name = "user_id")
        public Long userId;

        @Column(name = "claim_id")
        public Long claimId;
    }
}
