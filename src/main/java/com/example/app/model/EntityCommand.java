package com.example.app.model;

import com.example.app.converter.GeneralJsonConverter;
import jakarta.persistence.*;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "Commands")
public class EntityCommand {

    @Id
    private String id; 
    
    @Column(name = "superapp")
    private String superapp;

    @Column(name = "miniAppName")
    private String miniAppName;

    @Column(name = "command")
    private String command;

    @Column(name = "targetObjectSuperapp")
    private String targetObjectSuperapp;

    @Column(name = "targetObjectId")
    private String targetObjectId;

    @Column(name = "invocationTimestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date invocationTimestamp;

    @Column(name = "invokedBySuperapp")
    private String invokedBySuperapp;

    @Column(name = "invokedByEmail")
    private String invokedByEmail;

    @Column(name = "commandAttributes", columnDefinition = "varchar")
    private String commandAttributes; 

    public EntityCommand() {}

    public EntityCommand(String id, String superapp, String miniAppName, String command, 
                         String targetObjectSuperapp, String targetObjectId, Date invocationTimestamp, 
                         String invokedBySuperapp, String invokedByEmail, String commandAttributes) {
        this.id = id; 
        this.superapp = superapp;
        this.miniAppName = miniAppName;
        this.command = command;
        this.targetObjectSuperapp = targetObjectSuperapp;
        this.targetObjectId = targetObjectId;
        this.invocationTimestamp = invocationTimestamp;
        this.invokedBySuperapp = invokedBySuperapp;
        this.invokedByEmail = invokedByEmail;
        this.commandAttributes = commandAttributes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSuperapp() {
        return superapp;
    }

    public void setSuperapp(String superapp) {
        this.superapp = superapp;
    }

    public String getMiniapp() {
        return miniAppName;
    }

    public void setMiniapp(String miniAppName) {
        this.miniAppName = miniAppName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTargetObjectSuperapp() {
        return targetObjectSuperapp;
    }

    public void setTargetObjectSuperapp(String targetObjectSuperapp) {
        this.targetObjectSuperapp = targetObjectSuperapp;
    }

    public String getTargetObjectId() {
        return targetObjectId;
    }

    public void setTargetObjectId(String targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    public Date getInvocationTimestamp() {
        return invocationTimestamp;
    }

    public void setInvocationTimestamp(Date invocationTimestamp) {
        this.invocationTimestamp = invocationTimestamp;
    }

    public String getInvokedBySuperapp() {
        return invokedBySuperapp;
    }

    public void setInvokedBySuperapp(String invokedBySuperapp) {
        this.invokedBySuperapp = invokedBySuperapp;
    }

    public String getInvokedByEmail() {
        return invokedByEmail;
    }

    public void setInvokedByEmail(String invokedByEmail) {
        this.invokedByEmail = invokedByEmail;
    }

    public String getCommandAttributes() {
        return commandAttributes;
    }

    public void setCommandAttributes(String commandAttributes) {
        this.commandAttributes = commandAttributes;
    }
}
