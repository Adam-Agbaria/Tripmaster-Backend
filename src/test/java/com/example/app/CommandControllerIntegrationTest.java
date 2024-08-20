package com.example.app;

import com.example.app.converter.CommandConverter;
import com.example.app.converter.ObjectConverter;
import com.example.app.converter.UserConverter;
import com.example.app.model.BoundaryCommand;
import com.example.app.model.BoundaryObject;
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
public class CommandControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseUrl = "http://localhost:8084";

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EntityCommandRepository commandRepository;
    
    @Autowired
    private EntityObjectRepository objectRepository;
    
    @Autowired
    private CommandConverter commandConverter;
    
    @Autowired
    private UserConverter userConverter;

    @Autowired
    private ObjectConverter objectConverter;
    
    private List<UserBoundary> users;
    
    private List<BoundaryCommand> createdCommands;
    
    private List<BoundaryObject> createdObjects;

    
    ObjectMapper mapper = new ObjectMapper();


    @BeforeAll
    public void setup() {
        users = new ArrayList<>();
        createdCommands = new ArrayList<>();
        createdObjects = new ArrayList<>();
        registerAndSaveUser("superappTester@example.com", UserRole.SUPERAPP_USER);
        registerAndSaveUser("miniappTester@example.com", UserRole.MINIAPP_USER);
        registerAndSaveUser("adminTester@example.com", UserRole.ADMIN);

//        createAndSaveCommand("testApp");
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
        createdObjects.forEach(object -> objectRepository.deleteById(objectConverter.toEntity(object).getObjectId()));

    }

    @Test
    @Order(1)
    public void testFetchObjectsByAliasAndTypeCommand() {
        // Miniapp User - Should be allowed with existing object
        BoundaryCommand command = new BoundaryCommand();
        command.setCommandId(new BoundaryCommand.CommandId("tripMaster", "userApp", UUID.randomUUID().toString()));
        command.setCommand("fetchObjectsByAliasAndType");

        // Create and save a target object
        BoundaryObject targetObject = createTargetObject("tripMaster");
        command.setTargetObject(new BoundaryCommand.TargetObject(new BoundaryCommand.TargetObject.ObjectId("tripMaster", targetObject.getObjectId().getId())));

        BoundaryCommand.InvokedBy invokedBy = new BoundaryCommand.InvokedBy();
        invokedBy.setUserId(new BoundaryCommand.InvokedBy.UserId("tripMaster", "miniappTester@example.com"));
        command.setInvokedBy(invokedBy);

        command.setInvocationTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("alias", targetObject.getAlias());
        attributes.put("type", targetObject.getType());
        command.setCommandAttributes(attributes);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoundaryCommand createdCommand = mapper.convertValue(response.getBody().get(0), BoundaryCommand.class);
        createdCommands.add(createdCommand);

        // Miniapp User - Should be forbidden with non-existing object
        command.setTargetObject(new BoundaryCommand.TargetObject(new BoundaryCommand.TargetObject.ObjectId("tripMaster", UUID.randomUUID().toString())));
        response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Superapp User - Should be forbidden
        command.setInvokedBy(new BoundaryCommand.InvokedBy(new BoundaryCommand.InvokedBy.UserId("tripMaster", "superappTester@example.com")));
        response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Admin User - Should be forbidden
        command.setInvokedBy(new BoundaryCommand.InvokedBy(new BoundaryCommand.InvokedBy.UserId("tripMaster", "adminTester@example.com")));
        response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    
    @Test
    @Order(2)
    public void testFindObjectsByCreatorEmailAndTypeCommand() {
        // Miniapp User - Should be allowed with existing objects
        BoundaryCommand command = new BoundaryCommand();
        command.setCommandId(new BoundaryCommand.CommandId("tripMaster", "userApp", UUID.randomUUID().toString()));
        command.setCommand("findObjectsByCreatorEmailAndType");

        // Create and save a target object
        BoundaryObject targetObject = createTargetObject("tripMaster");
        command.setTargetObject(new BoundaryCommand.TargetObject(new BoundaryCommand.TargetObject.ObjectId("tripMaster", targetObject.getObjectId().getId())));

        BoundaryCommand.InvokedBy invokedBy = new BoundaryCommand.InvokedBy();
        invokedBy.setUserId(new BoundaryCommand.InvokedBy.UserId("tripMaster", "miniappTester@example.com"));
        command.setInvokedBy(invokedBy);

        command.setInvocationTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "miniappTester@example.com");
        attributes.put("type", "TestType");
        command.setCommandAttributes(attributes);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        // Log the actual response status and body for debugging
        System.out.println("Actual Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());

        // Assert the status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        BoundaryCommand createdCommand = mapper.convertValue(response.getBody().get(0), BoundaryCommand.class);
        createdCommands.add(createdCommand);

        List<Map<String, Object>> results = (List<Map<String, Object>>) createdCommand.getCommandAttributes().get("results");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).get("type")).isEqualTo("TestType");

        // Miniapp User - Should handle missing email
        command.getCommandAttributes().remove("email");
        response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        System.out.println("Actual Status Code for Missing Email: " + response.getStatusCode());
        System.out.println("Response Body for Missing Email: " + response.getBody());

        BoundaryCommand errorCommand = mapper.convertValue(response.getBody().get(0), BoundaryCommand.class);
        assertThat(errorCommand.getCommandAttributes().get("error")).isEqualTo("Email is missing or empty");

        // Miniapp User - Should handle missing type
        command.getCommandAttributes().put("email", "miniappTester@example.com");
        command.getCommandAttributes().remove("type");
        response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        System.out.println("Actual Status Code for Missing Type: " + response.getStatusCode());
        System.out.println("Response Body for Missing Type: " + response.getBody());

        errorCommand = mapper.convertValue(response.getBody().get(0), BoundaryCommand.class);
        assertThat(errorCommand.getCommandAttributes().get("error")).isEqualTo("Type is missing or empty");
    }

    @Test
    @Order(3)
    public void testFindObjectsByCommandAttributesEmailAndTypeCommand() {
        // Miniapp User - Should be allowed with existing objects
        BoundaryCommand command = new BoundaryCommand();
        command.setCommandId(new BoundaryCommand.CommandId("tripMaster", "userApp", UUID.randomUUID().toString()));
        command.setCommand("findObjectsByCommandAttributesEmailAndType");

        // Create and save a target object with specific email in objectDetails
        BoundaryObject targetObject = createTargetObject("tripMaster");
        command.setTargetObject(new BoundaryCommand.TargetObject(new BoundaryCommand.TargetObject.ObjectId("tripMaster", targetObject.getObjectId().getId())));

        BoundaryCommand.InvokedBy invokedBy = new BoundaryCommand.InvokedBy();
        invokedBy.setUserId(new BoundaryCommand.InvokedBy.UserId("tripMaster", "miniappTester@example.com"));
        command.setInvokedBy(invokedBy);

        command.setInvocationTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "miniappTester@example.com");
        attributes.put("type", "TestType");
        command.setCommandAttributes(attributes);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        // Log the actual response status and body for debugging
        System.out.println("Actual Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());

        // Assert the status code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        BoundaryCommand createdCommand = mapper.convertValue(response.getBody().get(0), BoundaryCommand.class);
        createdCommands.add(createdCommand);

        List<Map<String, Object>> results = (List<Map<String, Object>>) createdCommand.getCommandAttributes().get("results");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).get("type")).isEqualTo("TestType");

        // Miniapp User - Should handle missing email
        command.getCommandAttributes().remove("email");
        response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        System.out.println("Actual Status Code for Missing Email: " + response.getStatusCode());
        System.out.println("Response Body for Missing Email: " + response.getBody());

        BoundaryCommand errorCommand = mapper.convertValue(response.getBody().get(0), BoundaryCommand.class);
        assertThat(errorCommand.getCommandAttributes().get("error")).isEqualTo("Email is missing or empty");

        // Miniapp User - Should handle missing type
        command.getCommandAttributes().put("email", "miniappTester@example.com");
        command.getCommandAttributes().remove("type");
        response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/userApp",
                command,
                JsonNode.class
        );

        System.out.println("Actual Status Code for Missing Type: " + response.getStatusCode());
        System.out.println("Response Body for Missing Type: " + response.getBody());

        errorCommand = mapper.convertValue(response.getBody().get(0), BoundaryCommand.class);
        assertThat(errorCommand.getCommandAttributes().get("error")).isEqualTo("Type is missing or empty");
    }
     

    private BoundaryObject createTargetObject(String superapp) {
        BoundaryObject targetObject = new BoundaryObject();
        targetObject.setObjectId(new BoundaryObject.ObjectIdBoundary(superapp, UUID.randomUUID().toString()));
        targetObject.setType("TestType");
        targetObject.setAlias("TestAlias");
        targetObject.setLocation(new BoundaryObject.LocationBoundary(0, 0));
        targetObject.setCreatedBy(new BoundaryObject.CreatedByBoundary(new BoundaryObject.CreatedByBoundary.UserIdBoundary(superapp, "miniappTester@example.com")));
        targetObject.setActive(true);
        Map<String, Object> objectDetails = new HashMap<>();
        objectDetails.put("email", "miniappTester@example.com");
        objectDetails.put("message", "Your flight to paris has been canceled");
        targetObject.setObjectDetails(objectDetails);
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
                UserBoundary.class
        );

        return response.getBody();
    }

    private void createAndSaveCommand(String miniAppName) {
        BoundaryCommand command = new BoundaryCommand();
        command.setCommandId(new BoundaryCommand.CommandId("tripMaster", miniAppName, UUID.randomUUID().toString()));
        command.setCommand("Test Command");

        BoundaryCommand.TargetObject targetObject = new BoundaryCommand.TargetObject();
        targetObject.setObjectId(new BoundaryCommand.TargetObject.ObjectId("tripMaster", UUID.randomUUID().toString()));
        command.setTargetObject(targetObject);

        BoundaryCommand.InvokedBy invokedBy = new BoundaryCommand.InvokedBy();
        invokedBy.setUserId(new BoundaryCommand.InvokedBy.UserId("tripMaster", "miniappTester@example.com"));
        command.setInvokedBy(invokedBy);

        command.setInvocationTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("key1", "value1");
        attributes.put("key2", "value2");
        command.setCommandAttributes(attributes);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                baseUrl + "/superapp/miniapp/" + miniAppName,
                command,
                JsonNode.class
        );

//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BoundaryCommand createdCommand = mapper.convertValue(response.getBody().get(0), BoundaryCommand.class);

        createdCommands.add(createdCommand);
    }
}
