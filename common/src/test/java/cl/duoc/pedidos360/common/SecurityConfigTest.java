package cl.duoc.pedidos360.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.junit.jupiter.api.Test;

class SecurityConfigTest {
    private static final String AUD = "api://e17d6c18-3e48-4533-afb4-6c5c0cebe145";

    private static boolean ok(Object aud) {
        var b = Jwt.withTokenValue("t").header("alg", "none");
        if (aud != null) b.claim("aud", aud);
        else b.claim("sub", "x");
        return !SecurityConfig.audienceValidator(AUD).validate(b.build()).hasErrors();
    }

    @Test
    void audience() {
        assertTrue(ok(List.of(AUD)));                                        // token v1
        assertTrue(ok(List.of("e17d6c18-3e48-4533-afb4-6c5c0cebe145")));     // token v2 (GUID)
        assertFalse(ok(List.of("api://otra-app")));                          // otra API
        assertFalse(ok(null));                                               // sin aud
    }
}
