package com.example.app;

import com.example.app.converter.UserConverter;
import com.example.app.model.NewUserBoundary;
import com.example.app.model.UserBoundary;
import com.example.app.model.UserRole;
import com.example.app.repository.UserRepository;
import com.example.app.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = TripMaster3Application.class)
@TestPropertySource(locations = "classpath:application.properties")
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseUrl = "http://localhost:8084";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserConverter userConverter;
    @Autowired
    private UserService userService;

    private List<UserBoundary> users;

    @BeforeAll
    public void setup() {
        users = new ArrayList<>();
        registerAndSaveUser("adminTester@example.com", UserRole.ADMIN);
        registerAndSaveUser("superappTester@example.com", UserRole.SUPERAPP_USER);
        registerAndSaveUser("miniappTester@example.com", UserRole.MINIAPP_USER);
    }

    private void registerAndSaveUser(String email, UserRole role) {
        UserBoundary user = createUser("tripMaster", email, role);
        if (user != null && user.getUserId() != null) {
            users.add(user);
        }
    }

    @AfterAll
    public void terminator() {
        users.forEach(user -> userRepository.deleteById(userConverter.toEntity(user).getUserId()));
    }

    @Test
    @Order(1)
    public void testCreateUser() {
    	NewUserBoundary newUser = new NewUserBoundary();
        newUser.setEmail("newUser@example.com");
        newUser.setRole(UserRole.SUPERAPP_USER);
        newUser.setUsername("New User");
        newUser.setAvatar("avatar.png");

        ResponseEntity<UserBoundary> response = restTemplate.postForEntity(
                baseUrl + "/superapp/users",
                newUser,
                UserBoundary.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId().getEmail()).isEqualTo("newUser@example.com");

        userRepository.deleteById(userConverter.toEntity(response.getBody()).getUserId());
        
        // Invalid email format scenario
        NewUserBoundary invalidEmailUser = new NewUserBoundary();
        invalidEmailUser.setEmail("invalidEmailFormat");
        invalidEmailUser.setRole(UserRole.SUPERAPP_USER);
        invalidEmailUser.setUsername("Invalid Email User");
        invalidEmailUser.setAvatar("avatar.png");

        ResponseEntity<UserBoundary> invalidEmailResponse = restTemplate.postForEntity(
                baseUrl + "/superapp/users",
                invalidEmailUser,
                UserBoundary.class
        );

        assertThat(invalidEmailResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Order(2)
    public void testUpdateUser() {
        UserBoundary updatedUser = new UserBoundary(
                new UserBoundary.UserIdBoundary("tripMaster", "adminTester@example.com"),
                UserRole.ADMIN,
                "Updated Admin",
                "updated_avatar.png"
        );

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/superapp/users/tripMaster/adminTester@example.com",
                HttpMethod.PUT,
                new HttpEntity<>(updatedUser),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserBoundary user = userService.getUserById("tripMaster", "adminTester@example.com").orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("Updated Admin");
        
        // Non-existent user update scenario
        UserBoundary nonExistentUser = new UserBoundary(
                new UserBoundary.UserIdBoundary("tripMaster", "nonExistentUser@example.com"),
                UserRole.ADMIN,
                "Non-existent User",
                "nonexistent_avatar.png"
        );
        
        response = restTemplate.exchange(
                baseUrl + "/superapp/users/tripMaster/nonExistentUser@example.com",
                HttpMethod.PUT,
                new HttpEntity<>(nonExistentUser),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(3)
    public void testGetUser() {
        ResponseEntity<UserBoundary> response = restTemplate.exchange(
                baseUrl + "/superapp/users/login/tripMaster/adminTester@example.com",
                HttpMethod.GET,
                null,
                UserBoundary.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId().getEmail()).isEqualTo("adminTester@example.com");
        
        // Non-existent user scenario
        response = restTemplate.exchange(
                baseUrl + "/superapp/users/login/tripMaster/nonExistentUser@example.com",
                HttpMethod.GET,
                null,
                UserBoundary.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private UserBoundary createUser(String superapp, String email, UserRole role) {
        NewUserBoundary newUser = new NewUserBoundary();
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setUsername("Test User");
        newUser.setAvatar("avatar.png");

        ResponseEntity<UserBoundary> response = restTemplate.postForEntity(
                baseUrl + "/superapp/users",
                newUser,
                UserBoundary.class
        );

        return response.getBody();
    }
}
