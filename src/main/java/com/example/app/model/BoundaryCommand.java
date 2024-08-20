package com.example.app.model;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

public class BoundaryCommand {

    private CommandId commandId;
    private String command;
    private TargetObject targetObject;
    private String invocationTimestamp;
    private InvokedBy invokedBy;
    private Map<String, Object> commandAttributes;

    public BoundaryCommand() {}

    public BoundaryCommand(CommandId commandId, String command, TargetObject targetObject, String invocationTimestamp, InvokedBy invokedBy, Map<String, Object> commandAttributes) {
        this.commandId = commandId;
        this.command = command;
        this.targetObject = targetObject;
        this.invocationTimestamp = invocationTimestamp;
        this.invokedBy = invokedBy;
        this.commandAttributes = commandAttributes;
    }

    public CommandId getCommandId() {
        return commandId;
    }

    public void setCommandId(CommandId commandId) {
        this.commandId = commandId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public TargetObject getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(TargetObject targetObject) {
        this.targetObject = targetObject;
    }

    public String getInvocationTimestamp() {
        return invocationTimestamp;
    }

    public void setInvocationTimestamp(String invocationTimestamp) {
        this.invocationTimestamp = invocationTimestamp;
    }

    public InvokedBy getInvokedBy() {
        return invokedBy;
    }

    public void setInvokedBy(InvokedBy invokedBy) {
        this.invokedBy = invokedBy;
    }

    public Map<String, Object> getCommandAttributes() {
        return commandAttributes;
    }

    public void setCommandAttributes(Map<String, Object> commandAttributes) {
        this.commandAttributes = commandAttributes;
    }

    public static class CommandId {
        private String superapp;
        private String miniapp;
        private String id;

        public CommandId() {}

        public CommandId(String superapp, String miniapp, String id) {
            this.superapp = superapp;
            this.miniapp = miniapp;
            this.id = id;
        }

        public String getSuperapp() {
            return superapp;
        }

        public void setSuperapp(String superapp) {
            this.superapp = superapp;
        }

        public String getMiniapp() {
            return miniapp;
        }

        public void setMiniapp(String miniapp) {
            this.miniapp = miniapp;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class TargetObject {
        private ObjectId objectId;

        public TargetObject() {}

        public TargetObject(ObjectId objectId) {
            this.objectId = objectId;
        }

        public ObjectId getObjectId() {
            return objectId;
        }

        public void setObjectId(ObjectId objectId) {
            this.objectId = objectId;
        }

        public static class ObjectId {
            private String superapp;
            private String id;

            public ObjectId() {}

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
    }

    public static class InvokedBy {
        private UserId userId;

        public InvokedBy() {}

        public InvokedBy(UserId userId) {
            this.userId = userId;
        }

        public UserId getUserId() {
            return userId;
        }

        public void setUserId(UserId userId) {
            this.userId = userId;
        }

        public static class UserId {
            private String superapp;
            private String email;

            public UserId() {}

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
