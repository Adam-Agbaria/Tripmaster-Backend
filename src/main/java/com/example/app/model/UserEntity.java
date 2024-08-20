package com.example.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;


@Entity
@Table(name = "Users")
public class UserEntity {

    @EmbeddedId
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;

    @Column(name = "username")
    private String username;

    @Column(name = "avatar")
    private String avatar;

    public UserEntity() {
    }

    public UserEntity(UserId userId, UserRole role, String username, String avatar) {
        this.userId = userId;
        this.role = role;
        this.username = username;
        this.avatar = avatar;
    }

    public UserId getUserId() {
        return userId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Embeddable
    public static class UserId {

        private String superapp;

        @Column(name = "email")
        private String email;

        public UserId() {
        }

        public UserId(String superapp, String email) {
            this.superapp = superapp;
            this.email = email;
        }

        public String getSuperapp() {
            return superapp;
        }

        public void setSuperapp(String superapp) {
            this.superapp = superapp;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
