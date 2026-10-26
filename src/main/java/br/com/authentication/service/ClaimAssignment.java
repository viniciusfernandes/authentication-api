package br.com.authentication.service;

/**
 * Association of a claim (by id) with the value it must have for a user.
 */
public record ClaimAssignment(Long claimId, String value) {
}
