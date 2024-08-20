package com.example.app;

import com.example.app.converter.CommandConverter;
import com.example.app.converter.ObjectConverter;
import com.example.app.converter.UserConverter;
import com.example.app.model.BoundaryCommand;
import com.example.app.model.BoundaryObject;
import com.example.app.model.EntityCommand;
import com.example.app.model.NewUserBoundary;
import com.example.app.model.UserBoundary;
import com.example.app.model.UserRole;
import com.example.app.repository.EntityCommandRepository;
import com.example.app.repository.EntityObjectRepository;
import com.example.app.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = TripMaster3Application.class)
@TestPropertySource(locations = "classpath:application.properties")
@ActiveProfiles("local")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseUrl = "http://localhost:8084";

    @Autowired
    private EntityObjectRepository entityObjectRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserConverter userConverter;
    
    @Autowired
    private ObjectConverter objectConverter;
    
    @Autowired
    private CommandConverter commandConverter;

    @Autowired
    private EntityCommandRepository commandRepository;

    private List<UserBoundary> users;

    private List<BoundaryCommand> createdCommands;
    
    private List<BoundaryObject> createdObjects;

    
    ObjectMapper mapper = new ObjectMapper();


    @BeforeAll
    public void setup() {
        users = new ArrayList<>();
        createdCommands = new ArrayList<>();
        createdObjects = new ArrayList<>();

        registerAndSaveUser("adminTester@example.com", UserRole.ADMIN);
        registerAndSaveUser("superappTester@example.com", UserRole.SUPERAPP_USER);
        registerAndSaveUser("miniappTester@example.com", UserRole.MINIAPP_USER);
        createAndSaveCommand("userApp");
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
        createdCommands.forEach(command -> commandRepository.deleteById(commandConverter.toEntity(command).getId()));
        createdObjects.forEach(object -> entityObjectRepository.deleteById(objectConverter.toEntity(object).getObjectId()));

    }
    
    



    @Test
    @Order(1)
    public void testDeleteAllObjects() {
        ResponseEntity<Void> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/objects?userSuperapp=tripMaster&userEmail=adminTester@example.com",
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> superappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/objects?userSuperapp=tripMaster&userEmail=superappTester@example.com",
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Void> miniappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/objects?userSuperapp=tripMaster&userEmail=miniappTester@example.com",
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(miniappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(4)
    public void testDeleteAllCommands() {
        ResponseEntity<Void> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/miniapp?userSuperapp=tripMaster&userEmail=adminTester@example.com",
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> superappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/miniapp?userSuperapp=tripMaster&userEmail=superappTester@example.com",
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<Void> miniappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/miniapp?userSuperapp=tripMaster&userEmail=miniappTester@example.com",
                HttpMethod.DELETE,
                null,
                Void.class);
        assertThat(miniappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(5)
    public void testGetAllUsers() {
        ResponseEntity<List<UserBoundary>> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/users?userSuperapp=tripMaster&userEmail=adminTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UserBoundary>>() {});
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(adminResponse.getBody()).isNotEmpty();

        ResponseEntity<String> superappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/users?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<String> miniappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/users?userSuperapp=tripMaster&userEmail=miniappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(miniappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(3)
    public void testExportAllCommandsHistory() {
        ResponseEntity<List<BoundaryCommand>> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/miniapp?userSuperapp=tripMaster&userEmail=adminTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryCommand>>() {});
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(adminResponse.getBody()).isNotEmpty();

        ResponseEntity<String> superappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/miniapp?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<String> miniappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/miniapp?userSuperapp=tripMaster&userEmail=miniappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(miniappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(4)
    public void testExportCommandsHistoryByMiniApp() {
        ResponseEntity<List<BoundaryCommand>> adminResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/miniapp/userApp?userSuperapp=tripMaster&userEmail=adminTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<BoundaryCommand>>() {});
        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(adminResponse.getBody()).isNotEmpty();

        ResponseEntity<String> superappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/miniapp/userApp?userSuperapp=tripMaster&userEmail=superappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<String> miniappResponse = restTemplate.exchange(
                baseUrl + "/superapp/admin/miniapp/userApp?userSuperapp=tripMaster&userEmail=miniappTester@example.com&size=10&page=0",
                HttpMethod.GET,
                null,
                String.class);
        assertThat(miniappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    
//    @Test
//    @Order(6)
//    public void testDeleteAllUsers() {
//        ResponseEntity<Void> adminResponse = restTemplate.exchange(
//                baseUrl + "/superapp/admin/users?userSuperapp=tripMaster&userEmail=adminTester@example.com",
//                HttpMethod.DELETE,
//                null,
//                Void.class);
//        assertThat(adminResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
//
//        ResponseEntity<Void> superappResponse = restTemplate.exchange(
//                baseUrl + "/superapp/admin/users?userSuperapp=tripMaster&userEmail=superappTester@example.com",
//                HttpMethod.DELETE,
//                null,
//                Void.class);
//        assertThat(superappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
//
//        ResponseEntity<Void> miniappResponse = restTemplate.exchange(
//                baseUrl + "/superapp/admin/users?userSuperapp=tripMaster&userEmail=miniappTester@example.com",
//                HttpMethod.DELETE,
//                null,
//                Void.class);
//        assertThat(miniappResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
//        registerAndSaveUser("adminTester@example.com", UserRole.ADMIN);
//        registerAndSaveUser("superappTester@example.com", UserRole.SUPERAPP_USER);
//        registerAndSaveUser("miniappTester@example.com", UserRole.MINIAPP_USER);
//    }
//    
    private void createAndSaveCommand(String miniAppName) {
        BoundaryCommand command = new BoundaryCommand();
        command.setCommandId(new BoundaryCommand.CommandId("tripMaster", miniAppName, UUID.randomUUID().toString()));
        command.setCommand("fetchObjectsByAliasAndType");

        // Create and save a target object
        BoundaryObject targetObject = createTargetObject("tripMaster");
        command.setTargetObject(new BoundaryCommand.TargetObject(new BoundaryCommand.TargetObject.ObjectId("tripMaster", targetObject.getObjectId().getId())));

        BoundaryCommand.InvokedBy invokedBy = new BoundaryCommand.InvokedBy();
        invokedBy.setUserId(new BoundaryCommand.InvokedBy.UserId("tripMaster", "miniappTester@example.com"));
        command.setInvokedBy(invokedBy);

        command.setInvocationTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        command.setCommandAttributes(attributes);
        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/" + miniAppName,
                command,
                JsonNode.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            
            BoundaryCommand createdCommand = mapper.convertValue(response.getBody().get(0), BoundaryCommand.class);
            createdCommands.add(createdCommand);
            
            } catch (Exception e) {
            System.err.println("Failed to create command: " + e.getMessage());
            throw e;
        }

    }

    private BoundaryObject createTargetObject(String superapp) {
        BoundaryObject targetObject = new BoundaryObject();
        targetObject.setObjectId(new BoundaryObject.ObjectIdBoundary(superapp, UUID.randomUUID().toString()));
        targetObject.setType("TestType");
        targetObject.setAlias("TestAlias");
        targetObject.setLocation(new BoundaryObject.LocationBoundary(0, 0));
        targetObject.setCreatedBy(new BoundaryObject.CreatedByBoundary(new BoundaryObject.CreatedByBoundary.UserIdBoundary(superapp, "miniappTester@example.com")));
        targetObject.setActive(true);
        targetObject.setObjectDetails(new HashMap<>());

        ResponseEntity<BoundaryObject> response = restTemplate.postForEntity(
                baseUrl + "/superapp/objects",
                targetObject,
                BoundaryObject.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoundaryObject createdObject = response.getBody();
        createdObjects.add(createdObject);
        return createdObject;
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
                UserBoundary.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }


}
