package com.example.app.serviceImpl;

import com.example.app.model.EntityObject;
import com.example.app.model.NewUserBoundary;
import com.example.app.model.UserBoundary;
import com.example.app.model.UserEntity;
import com.example.app.model.UserRole;
import com.example.app.repository.UserRepository;
import com.example.app.service.SendEmailService;
import com.example.app.service.UserService;
import com.example.app.converter.UserConverter;
import com.example.app.exception.CustomBadRequestException;
import com.example.app.exception.CustomNotFoundException;
import com.example.app.validator.EmailValidator;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final SendEmailService sendEmailService;
    private String superapp;

    @Value("${spring.application.name}")
	public void setSpringApplicationName(String superapp) {
		this.superapp = superapp;
	}

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserConverter userConverter, SendEmailService sendEmailService) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.sendEmailService = sendEmailService;
    }

    @Override
	@Transactional(readOnly = false)
    public UserBoundary createUser(NewUserBoundary user) {
    	UserBoundary userB = userConverter.toBoundary(user);
        validateUser(userB);
        UserEntity userEntity = userConverter.toEntity(userB);
        UserEntity savedUser = userRepository.save(userEntity);
        sendEmail(user.getEmail());
        return userConverter.toBoundary(savedUser);
    }

    @Override
	@Transactional(readOnly = false)
    public void updateUser(String superappName, String userId, UserBoundary user) {
    	if(superappName != null) {
    		if(!superappName.equals(superapp)) {
        		throw new CustomBadRequestException("Superapp incorrect");
    		}
    	}else {
    		throw new CustomBadRequestException("Superapp can't be null in the url");
    	}
        validateUpdatedUser(user);
        UserEntity.UserId userID = new UserEntity.UserId(superapp, userId); 
        Optional<UserEntity> existingEntityOpt = userRepository.findById(userID);
        if (existingEntityOpt.isPresent()) {
            UserEntity existingEntity = existingEntityOpt.get();
            if (user.getRole() != null) {
            	if((!user.getRole().equals(UserRole.ADMIN)) && (!user.getRole().equals(UserRole.SUPERAPP_USER)) && (!user.getRole().equals(UserRole.MINIAPP_USER))){
                    throw new IllegalArgumentException("Invalid user role: " + user.getRole());
            	} else {
                    existingEntity.setRole(user.getRole());
            	}
            }
            if (user.getUsername() != null) {
                existingEntity.setUsername(user.getUsername());
            }
            if (user.getAvatar() != null) {
                existingEntity.setAvatar(user.getAvatar());
            }
            userRepository.save(existingEntity);
        }else {
            throw new CustomNotFoundException("Could not find entity user by email: " + userID.getEmail());
        }
    }

    @Override
	@Transactional(readOnly = true)
    public Optional<UserBoundary> getUserById(String superappName, String email) {
    	if(superappName != null) {
    		if(!superappName.equals(superapp)) {
        		throw new CustomBadRequestException("Superapp incorrect");
    		}
    	}else {
    		throw new CustomBadRequestException("Superapp can't be null in the url");
    	}
        UserEntity.UserId userId = new UserEntity.UserId(superapp, email); 
        return userRepository.findById(userId)
                .map(userConverter::toBoundary);
    }
    
    public void sendEmail(String email) {
        sendEmailService.sendSimpleEmail(email, "Trip Master", "You have been successfuly registered to our System!");
    }

    private void validateUser(UserBoundary user) {
        if (user == null) {
            throw new CustomBadRequestException("User cannot be null.");
        }
        validateUserRole(user.getRole());
        validateUserFields(user);
        UserEntity.UserId userId = new UserEntity.UserId(superapp, user.getUserId().getEmail());
        if (userRepository.findById(userId).isPresent()) {
            throw new CustomBadRequestException("User with email " + user.getUserId().getEmail() + " already exists.");
        }
        
        if (!EmailValidator.validateEmailExists(user.getUserId().getEmail())) {
            throw new CustomBadRequestException("Email address does not exist: " + user.getUserId().getEmail());
        }
    }
    
    private void validateUpdatedUser(UserBoundary user) {
        if (user == null) {
            throw new CustomBadRequestException("User cannot be null.");
        }
        if (user.getUserId() == null) {
            throw new CustomBadRequestException("User ID cannot be null.");
        }
        if (user.getUserId().getSuperapp() == null || user.getUserId().getSuperapp().isEmpty()) {
            throw new CustomBadRequestException("User superapp cannot be null or empty.");
        }
        if (user.getUserId().getEmail() == null || user.getUserId().getEmail().isEmpty()) {
            throw new CustomBadRequestException("User email cannot be null or empty.");
        }
    }

    private void validateUserFields(UserBoundary user) {
        if (user.getUserId() == null) {
            throw new CustomBadRequestException("User ID cannot be null.");
        }
        if (user.getUserId().getEmail() == null || user.getUserId().getEmail().isEmpty()) {
            throw new CustomBadRequestException("User email cannot be null or empty.");
        }
        if (user.getRole() == null) {
            throw new CustomBadRequestException("User role cannot be null");
            //WE VALIDATE THAT THE ROLE IS VALID IN A DIFFERENT PLACE
        }
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new CustomBadRequestException("User username cannot be null or empty.");
        }
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            throw new CustomBadRequestException("User avatar cannot be null or empty.");
        }
    }
    
    private void validateUserRole(UserRole userRole) {
        if (userRole == null || !isValidUserRole(userRole)) {
            throw new CustomBadRequestException("Invalid user role: " + userRole);
        }
    }

    private boolean isValidUserRole(UserRole userRole) {
        for (UserRole role : UserRole.values()) {
            if (role.equals(userRole)) {
                return true;
            }
        }
        return false;
    }
    
  
}

//
//    @Override
//    public void deleteUserById(String email) {
//        UserEntity.UserId userId = new UserEntity.UserId(null, email); // superapp will be set in the UserConverter
//        userRepository.deleteById(userId);
//    }


