package br.com.authentication.usecase.claim.create.endpoint;

import br.com.authentication.service.IClaimService;
import br.com.authentication.usecase.claim.create.contract.ClaimResponse;
import br.com.authentication.usecase.claim.create.contract.CreateClaimRequest;
import br.com.authentication.usecase.claim.create.mapping.ClaimMapper;
import br.com.authentication.usecase.user.create.endpoint.SaveUserEndpoint.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class CreateClaimEndpoint {

    private final IClaimService claimService;
    private final ClaimMapper claimMapper;

    // Requires an authenticated caller (anything outside /api/auth/** does).
    @PostMapping
    public ResponseEntity<ApiResponse<ClaimResponse>> createClaim(@Valid @RequestBody CreateClaimRequest request) {
        var claim = claimService.create(request.name, request.description, request.valueType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(claimMapper.toResponse(claim)));
    }
}
