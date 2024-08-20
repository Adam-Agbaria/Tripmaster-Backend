package com.example.app.serviceImpl;

import com.example.app.model.BoundaryCommand;
import com.example.app.model.EntityCommand;
import com.example.app.model.UserBoundary;
import com.example.app.model.UserEntity;
import com.example.app.model.UserRole;
import com.example.app.repository.EntityCommandRepository;
import com.example.app.repository.EntityObjectRepository;
import com.example.app.repository.UserRepository;
import com.example.app.updatedService.UpdatedAdminService;
import com.example.app.converter.CommandConverter;
import com.example.app.converter.UserConverter;
import com.example.app.exception.CustomBadRequestException;
import com.example.app.exception.CustomNotAuthorizedOpperation;
import com.example.app.exception.UnsupportedOperationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements UpdatedAdminService {

    private final EntityCommandRepository commandRepository;
    private final UserRepository userRepository;
    private final EntityObjectRepository entityObjectRepository;
    private final CommandConverter commandConverter;
    private final UserConverter userConverter;

    @Autowired
    public AdminServiceImpl(EntityCommandRepository commandRepository, UserRepository userRepository,
                            CommandConverter commandConverter, UserConverter userConverter, EntityObjectRepository entityObjectRepository) {
        this.commandRepository = commandRepository;
        this.userRepository = userRepository;
        this.entityObjectRepository = entityObjectRepository;
        this.commandConverter = commandConverter;
        this.userConverter = userConverter;
    }

    @Override
	@Transactional(readOnly = true)
    public List<BoundaryCommand> exportAllCommandsHistory() {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported.");

    }

    @Override
	@Transactional(readOnly = true)
    public List<BoundaryCommand> exportCommandsHistoryByMiniApp(String miniAppName) {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported.");

    }

    @Override
	@Transactional(readOnly = false)
    public void deleteAllCommandsHistory() {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported.");
    }

    @Override
	@Transactional(readOnly = true)
    public List<UserBoundary> getAllUsers() {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported.");

    }

    @Override
	@Transactional(readOnly = false)
    public void deleteAllUsers() {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported.");
    }
    
    @Override
	@Transactional(readOnly = false)
    public void deleteAllObjects() {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported.");
    }
    
    @Override
    @Transactional(readOnly = false)
    public void deleteAllUsers(String userSuperapp, String userEmail) {
        validateAdmin(userSuperapp, userEmail);
        userRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteAllObjects(String userSuperapp, String userEmail) {
        validateAdmin(userSuperapp, userEmail);
        entityObjectRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAllCommands(String userSuperapp, String userEmail) {
        validateAdmin(userSuperapp, userEmail);
        commandRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBoundary> getAllUsers(String userSuperapp, String userEmail, int size, int page) {
        validateAdmin(userSuperapp, userEmail);
        Page<UserEntity> usersPage = userRepository.findAll(PageRequest.of(page, size));
        return usersPage.stream().map(userConverter::toBoundary).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoundaryCommand> exportAllCommandsHistory(String userSuperapp, String userEmail, int size, int page) {
        validateAdmin(userSuperapp, userEmail);
        Page<EntityCommand> commandsPage = commandRepository.findAll(PageRequest.of(page, size));
        return commandsPage.stream().map(commandConverter::toBoundary).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoundaryCommand> exportCommandsHistoryByMiniApp(String miniAppName, String userSuperapp, String userEmail, int size, int page) {
        validateAdmin(userSuperapp, userEmail);
        Page<EntityCommand> commandsPage = commandRepository.findByMiniAppName(miniAppName, PageRequest.of(page, size));
        return commandsPage.stream().map(commandConverter::toBoundary).collect(Collectors.toList());
    }

    private void validateAdmin(String userSuperapp, String userEmail) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(new UserEntity.UserId(userSuperapp, userEmail));
        if (!userEntityOptional.isPresent() || userEntityOptional.get().getRole() != UserRole.ADMIN) {
            throw new CustomNotAuthorizedOpperation("User does not have the required permissions");
        }
    }

}
