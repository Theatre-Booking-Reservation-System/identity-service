package com.theatre.identityservice.security;

import com.theatre.identityservice.service.TokenService;
import com.theatre.identityservice.util.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Guards the role-claim -> authority mapping in {@link com.theatre.identityservice.config.JwtAuthenticationFilter}.
 *
 * <p>Real tokens carry the bare role name (e.g. "ADMIN"), while Spring's
 * {@code hasRole('ADMIN')} requires the "ROLE_ADMIN" authority. This test mints
 * a real ADMIN token and confirms an admin-only endpoint is NOT rejected with
 * 403 — reproducing the reported unlock-endpoint failure.
 */
@SpringBootTest
class JwtRoleAuthorizationIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TokenService tokenService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private String adminBearer() {
        UserDetails principal = User.withUsername("ADMIN:admin@sapumaltheatre.com")
                .password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
        return "Bearer " + tokenService.generateToken(
                principal, UserRole.ADMIN.name(), "admin@sapumaltheatre.com");
    }

    private String patronBearer() {
        UserDetails principal = User.withUsername("PATRON:patron@example.com")
                .password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PATRON")))
                .build();
        return "Bearer " + tokenService.generateToken(
                principal, UserRole.PATRON.name(), "patron@example.com");
    }

    /** An ADMIN token must be authorised for the admin-only unlock endpoint (not 403). */
    @Test
    void adminTokenIsAuthorisedForUnlock() throws Exception {
        mockMvc.perform(post("/patron/{id}/unlock", "16cd73af-d07d-4b80-9f23-990df238a152")
                        .header("Authorization", adminBearer()))
                // 404 (patron does not exist) proves we passed authorization;
                // the important assertion is that it is NOT 403.
                .andExpect(status().isNotFound());
    }

    /** An ADMIN token must be authorised for the admin-only list endpoint. */
    @Test
    void adminTokenIsAuthorisedForList() throws Exception {
        mockMvc.perform(get("/patron/list").header("Authorization", adminBearer()))
                .andExpect(status().isOk());
    }

    /** A PATRON token must be forbidden (403) on an admin-only endpoint. */
    @Test
    void patronTokenIsForbiddenForList() throws Exception {
        mockMvc.perform(get("/patron/list").header("Authorization", patronBearer()))
                .andExpect(status().isForbidden());
    }

    /** A PATRON token is allowed on the shared get-patron endpoint (404 = passed authz). */
    @Test
    void patronTokenIsAuthorisedForGetPatron() throws Exception {
        mockMvc.perform(get("/patron/{id}", "16cd73af-d07d-4b80-9f23-990df238a152")
                        .header("Authorization", patronBearer()))
                .andExpect(status().isNotFound());
    }
}
