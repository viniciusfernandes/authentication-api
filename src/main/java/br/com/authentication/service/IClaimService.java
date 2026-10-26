package br.com.authentication.service;

import br.com.authentication.domain.model.Claim;
import br.com.authentication.domain.model.ClaimValueType;

import java.util.List;

public interface IClaimService {

    Claim create(String name, String description, ClaimValueType valueType);

    List<Claim> findAll();
}
