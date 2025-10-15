package com.example.keycloakbackend;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static final KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:22.0.5")
        .withRealmImportFile("appx-realm.json");


    @DynamicPropertySource
    static void registerResourceServerIssuerProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> keycloak.getAuthServerUrl() + "/realms/appx-realm");
        registry.add("keycloak.auth-server-url", keycloak::getAuthServerUrl);
        registry.add("keycloak.realm", () -> "appx-realm");
        registry.add("keycloak.resource", () -> "appx-backend");
        registry.add("keycloak.credentials.secret", () -> "your-backend-client-secret");
    }

    private String getAccessToken(String username, String password) {
        return KeycloakBuilder.builder()
            .serverUrl(keycloak.getAuthServerUrl())
            .realm("appx-realm")
            .grantType(OAuth2Constants.PASSWORD)
            .clientId("appx-backend")
            .clientSecret("your-backend-client-secret")
            .username(username)
            .password(password)
            .build()
            .tokenManager()
            .getAccessToken()
            .getToken();
    }

    @Test
    void testPublicEndpoint() throws Exception {
        mockMvc.perform(get("/api/public/data"))
                .andExpect(status().isOk());
    }

    @Test
    void testSecureEndpoint_withAdminToken() throws Exception {
        mockMvc.perform(get("/api/secure/data")
                        .header("Authorization", "Bearer " + getAccessToken("adminuser", "admin")))
                .andExpect(status().isOk());
    }

    @Test
    void testSecureEndpoint_withNormalUserToken() throws Exception {
        mockMvc.perform(get("/api/secure/data")
                        .header("Authorization", "Bearer " + getAccessToken("normaluser", "user")))
                .andExpect(status().isOk());
    }

    @Test
    void testSecureEndpoint_withoutToken() throws Exception {
        mockMvc.perform(get("/api/secure/data"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminEndpoint_withAdminToken() throws Exception {
        mockMvc.perform(get("/api/admin/data")
                        .header("Authorization", "Bearer " + getAccessToken("adminuser", "admin")))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminEndpoint_withNormalUserToken() throws Exception {
        mockMvc.perform(get("/api/admin/data")
                        .header("Authorization", "Bearer " + getAccessToken("normaluser", "user")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminEndpoint_withoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/data"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminUsersEndpoint_withAdminToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + getAccessToken("adminuser", "admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].username", hasItem("adminuser")))
                .andExpect(jsonPath("$[*].username", hasItem("normaluser")))
                .andExpect(jsonPath("$[*].username", hasItem("service-account-appx-backend")));
    }

    @Test
    void testAdminUsersEndpoint_withNormalUserToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + getAccessToken("normaluser", "user")))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminUsersEndpoint_withoutToken() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }
}