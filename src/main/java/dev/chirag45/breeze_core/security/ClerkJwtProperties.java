package dev.chirag45.breeze_core.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "breeze.clerk")
public record ClerkJwtProperties(
        @NotBlank String issuerUri,
        @NotBlank String jwkSetUri,
        @NotEmpty List<String> allowedAuthorizedParties
) {
}
