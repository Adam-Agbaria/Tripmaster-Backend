package com.example.app.service;

import com.example.app.model.NewUserBoundary;
import com.example.app.model.UserBoundary;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserBoundary createUser(NewUserBoundary userBoundary);
    Optional<UserBoundary> getUserById(String superappName, String email);
    void updateUser(String superappName, String email, UserBoundary userBoundary);
//    void deleteUserById(String email);
}
