package com.theatre.identityservice.error;

import com.theatre.identityservice.util.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that every failure path returns the common
 * {@code {statusCode, statusDescription}} envelope.
 */
@SpringBootTest
class ErrorEnvelopeIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    /** Bean validation failure on register -> 400 with the envelope. */
    @Test
    void validationFailureReturnsEnvelope() throws Exception {
        mockMvc.perform(post("/patron/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\",\"password\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.VALIDATION_FAILED.getErrorCode()))
                .andExpect(jsonPath("$.statusDescription", notNullValue()));
    }

    /** Malformed JSON body -> 400 with the envelope. */
    @Test
    void malformedJsonReturnsEnvelope() throws Exception {
        mockMvc.perform(post("/patron/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.VALIDATION_FAILED.getErrorCode()))
                .andExpect(jsonPath("$.statusDescription", notNullValue()));
    }

    /** Wrong credentials on login -> 401 with the envelope. */
    @Test
    void invalidCredentialsReturnsEnvelope() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.com\",\"password\":\"whatever12\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.INVALID_USER_ID_OR_PASSWORD.getErrorCode()))
                .andExpect(jsonPath("$.statusDescription", notNullValue()));
    }

    /** Unauthenticated access to a protected endpoint -> 401 with the envelope. */
    @Test
    void unauthenticatedAccessReturnsEnvelope() throws Exception {
        mockMvc.perform(get("/patron/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.UNAUTHORIZED.getErrorCode()))
                .andExpect(jsonPath("$.statusDescription", notNullValue()));
    }

    /** Authenticated but wrong role (PATRON hitting an admin-only list) -> 403 envelope. */
    @Test
    @WithMockUser(authorities = "ROLE_PATRON")
    void forbiddenRoleReturnsEnvelope() throws Exception {
        mockMvc.perform(get("/patron/list"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.ACCESS_DENIED.getErrorCode()))
                .andExpect(jsonPath("$.statusDescription", notNullValue()));
    }

    /** Admin requesting a non-existent patron -> 404 with the envelope. */
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void patronNotFoundReturnsEnvelope() throws Exception {
        mockMvc.perform(get("/patron/{id}", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.PATRON_NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.statusDescription", notNullValue()));
    }

    /** Non-UUID path variable -> 400 with the envelope. */
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void badPathVariableReturnsEnvelope() throws Exception {
        mockMvc.perform(get("/patron/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.VALIDATION_FAILED.getErrorCode()))
                .andExpect(jsonPath("$.statusDescription", notNullValue()));
    }
}
