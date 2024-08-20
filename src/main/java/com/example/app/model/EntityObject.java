package com.example.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Temporal;



import org.springframework.beans.factory.annotation.Value;

import com.example.app.converter.CreatedByConverter;
import com.example.app.converter.ObjectDetailsConverter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "Objects")
public class EntityObject {

    @EmbeddedId
    private ObjectId objectId;

    @Column(name = "type")
    private String type;

    @Column(name = "alias")
    private String alias;

    @Column(name = "lat")
    private double lat;

    @Column(name = "lng")
    private double lng;

    @Column(name = "active")
    private boolean active;

    @Column(name = "creationTimestamp")
    @Temporal(TemporalType.TIMESTAMP) 
	private Date creationTimestamp;

    @Convert(converter = CreatedByConverter.class)
    @Column(name = "createdBy", columnDefinition = "TEXT")
    private CreatedBy createdBy;

    @Convert(converter = ObjectDetailsConverter.class)
    @Column(name = "objectDetails", columnDefinition = "TEXT")
    private Map<String, Object> objectDetails;

    // Constructors, getters, and setters

    public EntityObject() {
    }

    public EntityObject(ObjectId objectId, String type, String alias, double lat, double lng, boolean active, Date creationTimestamp, CreatedBy createdBy, Map<String, Object> objectDetails) {
        this.objectId = objectId;
        this.type = type;
        this.alias = alias;
        this.lat = lat;
        this.lng = lng;
        this.active = active;
        this.creationTimestamp = creationTimestamp;
        this.createdBy = createdBy;
        this.objectDetails = objectDetails;
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
    }

    public Map<String, Object> getObjectDetails() {
        return objectDetails;
    }

    public void setObjectDetails(Map<String, Object> objectDetails) {
        this.objectDetails = objectDetails;
    }

    @Embeddable
    public static class ObjectId {

        private String superapp;

        @Column(name = "id")
        private String id;

        public ObjectId() {
        }

        public ObjectId(String superapp, String id) {
            this.superapp = superapp;
            this.id = id;
        }

        public String getSuperapp() {
            return superapp;
        }

        public void setSuperapp(String superapp) {
            this.superapp = superapp;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }



    @Embeddable
    public static class CreatedBy {

        @Embedded
        private UserId userId;

        public CreatedBy() {
        }

        public CreatedBy(UserId userId) {
            this.userId = userId;
        }

        public UserId getUserId() {
            return userId;
        }

        public void setUserId(UserId userId) {
            this.userId = userId;
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
}
