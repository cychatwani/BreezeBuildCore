package dev.chirag45.breeze_core.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;

public final class AuthorizedPartyValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_AUTHORIZED_PARTY = new OAuth2Error(
            "invalid_token",
            "JWT azp claim is not an allowed BreezeBuild frontend.",
            null
    );

    private final Set<String> allowedAuthorizedParties;

    public AuthorizedPartyValidator(Set<String> allowedAuthorizedParties) {
        this.allowedAuthorizedParties = Set.copyOf(allowedAuthorizedParties);
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String authorizedParty = jwt.getClaimAsString("azp");

        if (authorizedParty == null || allowedAuthorizedParties.contains(authorizedParty)) {
            return OAuth2TokenValidatorResult.success();
        }

        return OAuth2TokenValidatorResult.failure(INVALID_AUTHORIZED_PARTY);
    }
}
