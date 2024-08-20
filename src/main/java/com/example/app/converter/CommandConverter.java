package com.example.app.converter;

import com.example.app.model.EntityCommand;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.app.model.BoundaryCommand;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Component
public class CommandConverter {

    private static final Logger logger = LoggerFactory.getLogger(CommandConverter.class);
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private final ObjectMapper objectMapper = new ObjectMapper();  


    public EntityCommand toEntity(BoundaryCommand boundaryCommand) {
        if (boundaryCommand == null) {
            logger.error("Received null BoundaryCommand for conversion.");
            return null;
        }

        EntityCommand entityCommand = new EntityCommand();
        entityCommand.setSuperapp(boundaryCommand.getCommandId().getSuperapp());
        entityCommand.setMiniapp(boundaryCommand.getCommandId().getMiniapp());
        entityCommand.setId(boundaryCommand.getCommandId().getId());
        entityCommand.setCommand(boundaryCommand.getCommand());
        entityCommand.setTargetObjectSuperapp(boundaryCommand.getTargetObject().getObjectId().getSuperapp());
        entityCommand.setTargetObjectId(boundaryCommand.getTargetObject().getObjectId().getId());
        entityCommand.setInvocationTimestamp(parseDate(boundaryCommand.getInvocationTimestamp()));
        entityCommand.setInvokedBySuperapp(boundaryCommand.getInvokedBy().getUserId().getSuperapp());
        entityCommand.setInvokedByEmail(boundaryCommand.getInvokedBy().getUserId().getEmail());
        try {
            String jsonAttributes = objectMapper.writeValueAsString(boundaryCommand.getCommandAttributes());
            entityCommand.setCommandAttributes(jsonAttributes);
        } catch (Exception e) {
            logger.error("Failed to serialize command attributes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize command attributes", e);
        }
        return entityCommand;
    }

    public BoundaryCommand toBoundary(EntityCommand entityCommand) {
        if (entityCommand == null) {
            logger.error("Received null EntityCommand for conversion.");
            return null;
        }

        BoundaryCommand boundaryCommand = new BoundaryCommand();
        BoundaryCommand.CommandId commandId = new BoundaryCommand.CommandId(
            entityCommand.getSuperapp(), entityCommand.getMiniapp(), entityCommand.getId());
        boundaryCommand.setCommandId(commandId);
        boundaryCommand.setCommand(entityCommand.getCommand());

        BoundaryCommand.TargetObject targetObject = new BoundaryCommand.TargetObject(
            new BoundaryCommand.TargetObject.ObjectId(entityCommand.getTargetObjectSuperapp(), entityCommand.getTargetObjectId()));
        boundaryCommand.setTargetObject(targetObject);

        String formattedDate = formatter.format(entityCommand.getInvocationTimestamp());
        boundaryCommand.setInvocationTimestamp(formattedDate);

        BoundaryCommand.InvokedBy.UserId userId = new BoundaryCommand.InvokedBy.UserId(
            entityCommand.getInvokedBySuperapp(), entityCommand.getInvokedByEmail());
        BoundaryCommand.InvokedBy invokedBy = new BoundaryCommand.InvokedBy(userId);
        boundaryCommand.setInvokedBy(invokedBy);

        try {
            Map<String, Object> attributesMap = objectMapper.readValue(entityCommand.getCommandAttributes(), new TypeReference<Map<String, Object>>() {});
            boundaryCommand.setCommandAttributes(attributesMap);
        } catch (Exception e) {
            logger.error("Failed to deserialize command attributes: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize command attributes", e);
        }
        return boundaryCommand;
    }


    private Date parseDate(String dateTimeStr) {
        try {
            return formatter.parse(dateTimeStr); 
        } catch (Exception e) {
            logger.error("Error parsing date: {}", dateTimeStr, e);
            return generateRandomDate();
        }
    }

    private Date generateRandomDate() {
        long randomMillis = ThreadLocalRandom.current().nextLong(
            System.currentTimeMillis() - (10L * 365 * 24 * 60 * 60 * 1000), 
            System.currentTimeMillis() + (10L * 365 * 24 * 60 * 60 * 1000)  
        );
        return new Date(randomMillis);
    }
    
    

}
