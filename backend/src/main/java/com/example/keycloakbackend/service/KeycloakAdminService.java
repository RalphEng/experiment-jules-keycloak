package com.example.keycloakbackend.service;

import com.example.keycloakbackend.dto.UserDTO;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakAdminService {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    public List<UserDTO> getUsers() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType("client_credentials")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        List<UserRepresentation> userRepresentations = keycloak.realm(realm).users().list();
        List<UserDTO> users = new ArrayList<>(userRepresentations.stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail()))
                .toList());

        List<ClientRepresentation> clients = keycloak.realm(realm).clients().findAll();
        for (ClientRepresentation client : clients) {
            if (client.isServiceAccountsEnabled()) {
                UserRepresentation serviceAccountUser = keycloak.realm(realm).clients().get(client.getId()).getServiceAccountUser();
                users.add(new UserDTO(serviceAccountUser.getId(), serviceAccountUser.getUsername(), serviceAccountUser.getEmail()));
            }
        }

        return users;
    }
}