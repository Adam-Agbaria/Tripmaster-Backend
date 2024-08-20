package com.example.app.serviceImpl;

import com.example.app.model.BoundaryCommand;
import com.example.app.model.BoundaryObject;
import com.example.app.model.EntityCommand;
import com.example.app.model.EntityObject;
import com.example.app.model.UserBoundary;
import com.example.app.model.UserRole;
import com.example.app.repository.EntityCommandRepository;
import com.example.app.service.CommandService;
import com.example.app.service.ObjectService;
import com.example.app.service.UserService;
import com.example.app.updatedService.UpdatedObjectService;
import com.example.app.converter.CommandConverter;
import com.example.app.exception.CustomBadRequestException;
import com.example.app.exception.CustomNotAuthorizedOpperation;
import com.example.app.miniapp.MiniAppCommand;
import com.example.app.miniapp.user.UserApp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommandServiceImpl implements CommandService {

    private final EntityCommandRepository entityCommandRepository;
    private final CommandConverter commandConverter;
    private final UpdatedObjectService objectService;
    private final UserService userService;
    private final ApplicationContext applicationContext;
    private final UserApp userApp;
    private String superapp;
	private Log logger = LogFactory.getLog(CommandServiceImpl.class);

    @Value("${spring.application.name}")
	public void setSpringApplicationName(String superapp) {
		this.superapp = superapp;
	}
    
    @Autowired
    public CommandServiceImpl(EntityCommandRepository entityCommandRepository, CommandConverter commandConverter,
                              UpdatedObjectService objectService, UserService userService, UserApp userApp, ApplicationContext applicationContext) {
        this.entityCommandRepository = entityCommandRepository;
        this.commandConverter = commandConverter;
        this.objectService = objectService;
        this.userService = userService;
        this.userApp = userApp;
        this.applicationContext = applicationContext;
    }


 

    @Override
    @Transactional(readOnly = false)
    public BoundaryCommand createCommand(BoundaryCommand boundaryCommand, String miniAppName) {
        validateCommand(boundaryCommand);

        if (boundaryCommand.getCommandId() == null) {
            boundaryCommand.setCommandId(new BoundaryCommand.CommandId());
        }
        String uniqueCommandId;
        do {
            uniqueCommandId = UUID.randomUUID().toString();
        } while (entityCommandRepository.existsById(uniqueCommandId));

        boundaryCommand.getCommandId().setId(uniqueCommandId);
        boundaryCommand.getCommandId().setMiniapp(miniAppName);
        boundaryCommand.getCommandId().setSuperapp(superapp);
        boundaryCommand.setInvocationTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));

        // Find and execute the appropriate miniapp command
        MiniAppCommand miniAppCommand = userApp.getCommand(boundaryCommand.getCommand());
        BoundaryCommand executedCommand;
        try {
            executedCommand = miniAppCommand.execute(boundaryCommand);
        } catch (Exception e) {
            throw new CustomBadRequestException("Failed to execute miniApp command: " + e.getMessage());
        }

        EntityCommand entityCommand = commandConverter.toEntity(executedCommand);
        entityCommand.setId(uniqueCommandId);
        entityCommand.setInvocationTimestamp(new Date());

        EntityCommand savedEntityCommand;
        try {
            savedEntityCommand = entityCommandRepository.save(entityCommand);
        } catch (Exception e) {
        	this.logger.trace("** SQL Exception: " + e.getMessage());
            this.logger.trace("** Command Details: " + boundaryCommand.toString());
            throw new CustomBadRequestException("Failed to create command: " + e.getMessage());
        }

        return commandConverter.toBoundary(savedEntityCommand);
    }

    private void validateCommand(BoundaryCommand boundaryCommand) {
        if (boundaryCommand.getCommand() == null || boundaryCommand.getCommand().isEmpty()) {
            throw new CustomBadRequestException("Command can't be null or empty");
        }

        String invokedByUserId = boundaryCommand.getInvokedBy().getUserId().getEmail();
        String userSuperapp = boundaryCommand.getInvokedBy().getUserId().getSuperapp();
        Optional<UserBoundary> userOpt = userService.getUserById(userSuperapp, invokedByUserId);

        if (userOpt.isEmpty()) {
            throw new CustomBadRequestException("User ID mismatch: " + invokedByUserId);
        }

        UserBoundary user = userOpt.get();

        if (user.getRole() != UserRole.MINIAPP_USER) {
            throw new CustomNotAuthorizedOpperation("User is not authorized to perform this operation");
        }

        if (!userSuperapp.equals(superapp)) {
            throw new CustomBadRequestException("Superapp mismatch: user does not belong to this superapp");
        }

        BoundaryCommand.TargetObject targetObject = boundaryCommand.getTargetObject();
        if (targetObject == null || targetObject.getObjectId() == null) {
            throw new CustomBadRequestException("Target object must be specified");
        }

        String targetObjectSuperapp = targetObject.getObjectId().getSuperapp();
        String targetObjectId = targetObject.getObjectId().getId();

        if (!superapp.equals(targetObjectSuperapp)) {
            throw new CustomBadRequestException("Superapp mismatch in target object");
        }

        // Validate that the target object exists in the database
        objectService.getObject(targetObjectSuperapp, targetObjectId, userSuperapp, invokedByUserId);
    }
}