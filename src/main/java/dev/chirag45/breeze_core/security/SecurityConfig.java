package dev.chirag45.breeze_core.security;

import dev.chirag45.breeze_core.observability.RequestCorrelationFilter;
import dev.chirag45.breeze_core.observability.AuthenticatedUserMdcFilter;
import dev.chirag45.breeze_core.exception.ApiAccessDeniedHandler;
import dev.chirag45.breeze_core.exception.ApiAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(ClerkJwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CoreUserProvisioningFilter coreUserProvisioningFilter,
            RequestCorrelationFilter requestCorrelationFilter,
            AuthenticatedUserMdcFilter authenticatedUserMdcFilter,
            ApiAuthenticationEntryPoint apiAuthenticationEntryPoint,
            ApiAccessDeniedHandler apiAccessDeniedHandler
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/users/provision").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .jwt(Customizer.withDefaults())
                )
                .addFilterAfter(requestCorrelationFilter, SecurityContextHolderFilter.class)
                .addFilterAfter(authenticatedUserMdcFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(coreUserProvisioningFilter, AuthenticatedUserMdcFilter.class)
                .build();
    }

    @Bean
    public RequestCorrelationFilter requestCorrelationFilter(
            @Value("${breeze.observability.instance-id}") String instanceId
    ) {
        return new RequestCorrelationFilter(instanceId);
    }

    @Bean
    public AuthenticatedUserMdcFilter authenticatedUserMdcFilter() {
        return new AuthenticatedUserMdcFilter();
    }

    @Bean
    public JwtDecoder jwtDecoder(ClerkJwtProperties clerkJwtProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(clerkJwtProperties.jwkSetUri())
                .build();

        OAuth2TokenValidator<Jwt> subjectValidator = new JwtClaimValidator<String>(
                "sub",
                subject -> subject != null && !subject.isBlank()
        );

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(clerkJwtProperties.issuerUri()),
                new AuthorizedPartyValidator(Set.copyOf(clerkJwtProperties.allowedAuthorizedParties())),
                subjectValidator
        ));

        return jwtDecoder;
    }

    @Bean("coreUserRequiredRequestMatcher")
    public RequestMatcher coreUserRequiredRequestMatcher() {
        return request -> {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            boolean isProvisioningRequest = HttpMethod.POST.matches(request.getMethod())
                    && "/api/users/provision".equals(path);

            return path.startsWith("/api/") && !isProvisioningRequest;
        };
    }
}
