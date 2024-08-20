package com.example.app.converter;

import com.example.app.model.NewUserBoundary;
import com.example.app.model.UserBoundary;
import com.example.app.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

	private String superapp;

    @Value("${spring.application.name}")
	public void setSpringApplicationName(String superapp) {
		this.superapp = superapp;
	}


    public UserEntity toEntity(UserBoundary userBoundary) {
    	UserEntity entity = new UserEntity();
        UserEntity.UserId userId = new UserEntity.UserId(
            superapp,
            userBoundary.getUserId().getEmail()
        );
        entity.setUserId(userId);
        entity.setRole(userBoundary.getRole());
        entity.setUsername(userBoundary.getUsername());
        entity.setAvatar(userBoundary.getAvatar());

        return entity;
    }

    public UserBoundary toBoundary(UserEntity userEntity) {
    	UserBoundary boundary = new UserBoundary();
        UserBoundary.UserIdBoundary userIdBoundary = new UserBoundary.UserIdBoundary(
            userEntity.getUserId().getSuperapp(),
            userEntity.getUserId().getEmail()
        );
        boundary.setUserId(userIdBoundary);
        boundary.setAvatar(userEntity.getAvatar());
        boundary.setUsername(userEntity.getUsername());
        boundary.setRole(userEntity.getRole());

        return boundary;
    }
    
    public UserBoundary toBoundary(NewUserBoundary newUserBoundary) {
        UserBoundary boundary = new UserBoundary();
        UserBoundary.UserIdBoundary userIdBoundary = new UserBoundary.UserIdBoundary(
            superapp,
            newUserBoundary.getEmail()
        );
        boundary.setUserId(userIdBoundary);
        boundary.setAvatar(newUserBoundary.getAvatar());
        boundary.setUsername(newUserBoundary.getUsername());
        boundary.setRole(newUserBoundary.getRole());

        return boundary;
    }
}
