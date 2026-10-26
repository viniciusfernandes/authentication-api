package br.com.authentication.usecase.claim.list.endpoint;

import br.com.authentication.service.IClaimService;
import br.com.authentication.usecase.claim.create.contract.ClaimResponse;
import br.com.authentication.usecase.claim.create.mapping.ClaimMapper;
import br.com.authentication.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ListClaimsEndpoint {

    private final IClaimService claimService;
    private final ClaimMapper claimMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> listClaims() {
        var claims = claimService.findAll().stream()
                .map(claimMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(claims));
    }
}
