package com.example.app.converter;


import com.example.app.model.*;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ObjectConverter {
	
	private static final Logger logger = LoggerFactory.getLogger(CommandConverter.class);
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public EntityObject toEntity(BoundaryObject boundaryObject) {
		EntityObject entity= new EntityObject();

        EntityObject.ObjectId objectId = new EntityObject.ObjectId(
            boundaryObject.getObjectId().getSuperapp(), 
            boundaryObject.getObjectId().getId()
        );
        entity.setObjectId(objectId);
        if(boundaryObject.getLocation() != null) {
        entity.setLat(boundaryObject.getLocation().getLat());
        entity.setLng(boundaryObject.getLocation().getLng());
        } else {
        	entity.setLat(0.0);
            entity.setLng(0.0);
        }

        EntityObject.CreatedBy.UserId userId = new EntityObject.CreatedBy.UserId(
            boundaryObject.getCreatedBy().getUserId().getSuperapp(), 
            boundaryObject.getCreatedBy().getUserId().getEmail()
        );
        EntityObject.CreatedBy createdBy= new EntityObject.CreatedBy(userId);
        entity.setCreatedBy(createdBy);;
        
        Date creationTimestamp;
        if(boundaryObject.getCreationTimestamp() != null) {
        creationTimestamp = parseDate(boundaryObject.getCreationTimestamp());
        } else {
        	creationTimestamp = new Date();
        }
        
       entity.setCreationTimestamp(creationTimestamp);
       entity.setActive(boundaryObject.isActive());
       entity.setType(boundaryObject.getType());
       entity.setAlias(boundaryObject.getAlias());
       entity.setObjectDetails(boundaryObject.getObjectDetails());
       
        
        return entity;
    }

    public BoundaryObject toBoundary(EntityObject entityObject) {
    	BoundaryObject boundary = new BoundaryObject();
        BoundaryObject.ObjectIdBoundary objectIdBoundary = new BoundaryObject.ObjectIdBoundary(
            entityObject.getObjectId().getSuperapp(), 
            entityObject.getObjectId().getId()
        );
        
        BoundaryObject.LocationBoundary locationBoundary = new BoundaryObject.LocationBoundary(
            entityObject.getLat(), 
            entityObject.getLng()
        );
        
        BoundaryObject.CreatedByBoundary.UserIdBoundary userIdBoundary = new BoundaryObject.CreatedByBoundary.UserIdBoundary(
            entityObject.getCreatedBy().getUserId().getSuperapp(), 
            entityObject.getCreatedBy().getUserId().getEmail()
        );
        
        BoundaryObject.CreatedByBoundary createdByBoundary = new BoundaryObject.CreatedByBoundary(userIdBoundary);
        
        boundary.setObjectId(objectIdBoundary);
        boundary.setLocation(locationBoundary);
        boundary.setCreatedBy(createdByBoundary);
        boundary.setCreationTimestamp(entityObject.getCreationTimestamp().toString());
        boundary.setType(entityObject.getType());
        boundary.setObjectDetails(entityObject.getObjectDetails());
        boundary.setAlias(entityObject.getAlias());
        boundary.setActive(entityObject.isActive());
        

        return boundary;
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
