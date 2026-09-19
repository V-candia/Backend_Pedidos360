package cl.duoc.pedidos360.common;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/** JWT de Azure AD: valida issuer + audience y mapea el claim "roles" a ROLE_<rol>. */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
            @Value("${app.audience}") String audience,
            @Value("${app.jwk-set-uri:}") String jwkSetUri) { // solo para pruebas locales sin Azure (infra/local)
        NimbusJwtDecoder decoder = jwkSetUri.isEmpty()
                ? NimbusJwtDecoder.withIssuerLocation(issuer).build()
                : NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new JwtClaimValidator<List<String>>("aud", aud -> aud != null && aud.contains(audience))));
        return decoder;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint((req, res, e) ->
                                write(mapper, res, HttpStatus.UNAUTHORIZED, "Token inválido o expirado"))
                        .accessDeniedHandler((req, res, e) ->
                                write(mapper, res, HttpStatus.FORBIDDEN, "Rol sin permiso para este endpoint")))
                .build();
    }

    private static void write(ObjectMapper mapper, HttpServletResponse res, HttpStatus s, String msg) throws IOException {
        res.setStatus(s.value());
        res.setContentType("application/json");
        mapper.writeValue(res.getOutputStream(), new ApiError(s.value(), s.name(), msg, Instant.now()));
    }
}
