package com.example.app.serviceImpl;

import com.example.app.model.BoundaryObject;
import com.example.app.model.EntityObject;
import com.example.app.model.UserBoundary;
import com.example.app.model.UserEntity;
import com.example.app.model.UserRole;
import com.example.app.repository.EntityObjectRepository;
import com.example.app.service.ObjectService;
import com.example.app.service.UserService;
import com.example.app.updatedService.UpdatedObjectService;
import com.example.app.converter.ObjectConverter;
import com.example.app.exception.CustomBadRequestException;
import com.example.app.exception.CustomNotAuthorizedOpperation;
import com.example.app.exception.CustomNotFoundException;
import com.example.app.exception.UnsupportedOperationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ObjectServiceImpl implements UpdatedObjectService {

    private final EntityObjectRepository entityObjectRepository;
    private final ObjectConverter objectConverter;
    private final UserService userService;
    private String superapp;


    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Value("${spring.application.name}")
	public void setSpringApplicationName(String superapp) {
		this.superapp = superapp;
	}

    @Autowired
    public ObjectServiceImpl(EntityObjectRepository entityObjectRepository, ObjectConverter objectConverter, UserService userService) {
        this.entityObjectRepository = entityObjectRepository;
        this.objectConverter = objectConverter;
        this.userService = userService;
    }

    @Override
	@Transactional(readOnly = false)
    public BoundaryObject createObject(BoundaryObject boundaryObject) {
    	validateCreateObject(boundaryObject);
    	String uniqueId;
    	EntityObject.ObjectId objectId;
        do {
            uniqueId = UUID.randomUUID().toString();
            objectId = new EntityObject.ObjectId(superapp, uniqueId);

        } while (entityObjectRepository.existsById(objectId));
    	boundaryObject.getObjectId().setId(uniqueId);
    	boundaryObject.getObjectId().setSuperapp(superapp);
        boundaryObject.setCreationTimestamp(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
        
        
        EntityObject entityObject = objectConverter.toEntity(boundaryObject);
        
       

        EntityObject savedEntityObject = entityObjectRepository.save(entityObject);
        
        return objectConverter.toBoundary(savedEntityObject);
    }

    @Override
	@Transactional(readOnly = true)
    public Optional<BoundaryObject> getObject(String superappPassed, String id) {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported.");
    }
    
    @Override
    @Transactional(readOnly = true)
    public BoundaryObject getObject(String superapp, String id, String userSuperapp, String userEmail) {
        // Validate user
        UserBoundary user = userService.getUserById(userSuperapp, userEmail)
                .orElseThrow(() -> new CustomBadRequestException("User not found or not authorized"));

        EntityObject entityObject = entityObjectRepository.findById(new EntityObject.ObjectId(superapp, id))
                .orElseThrow(() -> new CustomNotFoundException("Object not found with ID: " + id));

        // Role-based access control
        if (user.getRole() == UserRole.SUPERAPP_USER) {
            return objectConverter.toBoundary(entityObject);
        } else if (user.getRole() == UserRole.MINIAPP_USER) {
            if (entityObject.isActive() == true) {
                return objectConverter.toBoundary(entityObject);
            } else {
                throw new CustomNotAuthorizedOpperation("User does not have the required permissions to access inactive objects");
            }
        } else {
            throw new CustomNotAuthorizedOpperation("User does not have the required permissions");
        }
    }


  
    @Override
	@Transactional(readOnly = false)
    public void updateObject(String superappPassed, String id, BoundaryObject boundaryObject) {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported.");

    }

    @Override
    @Transactional(readOnly = false)
    public void updateObject(String superapp, String id, String userSuperapp, String userEmail, BoundaryObject boundaryObject) {
    	
    	if (superapp != null) {
            if (!superapp.equals(superapp)) {
                throw new CustomBadRequestException("Superapp incorrect");
            }
        } else {
            throw new CustomBadRequestException("Superapp can't be null in the URL");
        }
    	
        UserBoundary user = userService.getUserById(userSuperapp, userEmail)
                .orElseThrow(() -> new CustomBadRequestException("User not found or not authorized"));

        validateUpdatedObject(boundaryObject);

        

        EntityObject.ObjectId objectId = new EntityObject.ObjectId(superapp, id);
        EntityObject existingEntity = entityObjectRepository.findById(objectId)
                .orElseThrow(() -> new CustomNotFoundException("Object not found with ID: " + id));

        if (user.getRole() == UserRole.SUPERAPP_USER || user.getRole() == UserRole.MINIAPP_USER) {
        	if (user.getRole() == UserRole.MINIAPP_USER && existingEntity.isActive() == false) {
                throw new CustomNotAuthorizedOpperation("User does not have the required permissions");
        	}
            if (boundaryObject.getType() != null) {
                existingEntity.setType(boundaryObject.getType());
            }
            if (boundaryObject.getAlias() != null) {
                existingEntity.setAlias(boundaryObject.getAlias());
            }
            if (boundaryObject.getLocation() != null) {
                double lat = boundaryObject.getLocation().getLat();
                double lng = boundaryObject.getLocation().getLng();
                if (lat < -90 || lat > 90) {
                    throw new CustomBadRequestException("Invalid latitude value: " + lat);
                }
                if (lng < -180 || lng > 180) {
                    throw new CustomBadRequestException("Invalid longitude value: " + lng);
                }
                existingEntity.setLat(lat);
                existingEntity.setLng(lng);
            }
                existingEntity.setActive(boundaryObject.isActive());
            if (boundaryObject.getObjectDetails() != null) {
                existingEntity.setObjectDetails(boundaryObject.getObjectDetails());
            }

            entityObjectRepository.save(existingEntity);
        } else {
            throw new CustomNotAuthorizedOpperation("User does not have the required permissions");
        }
    }

    
    

    
    @Override
    @Transactional(readOnly = true)
    public List<BoundaryObject> getAllObjects() {
        throw new UnsupportedOperationException("This method is deprecated and no longer supported.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoundaryObject> getAllObjects(String userSuperapp, String userEmail, int size, int page) {
        UserBoundary user = userService.getUserById(userSuperapp, userEmail)
                .orElseThrow(() -> new CustomBadRequestException("User not found"));

        List<EntityObject> entityObjects;

        if (user.getRole() == UserRole.SUPERAPP_USER) {
            entityObjects = entityObjectRepository.findAll(PageRequest.of(page, size, Direction.ASC, "objectId")).getContent();
        } else if (user.getRole() == UserRole.MINIAPP_USER) {
            entityObjects = entityObjectRepository.findAllActive(PageRequest.of(page, size, Direction.ASC, "objectId")).getContent();
        } else {
            throw new CustomNotAuthorizedOpperation("User does not have the required permissions");
        }

        return entityObjects.stream().map(objectConverter::toBoundary).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BoundaryObject> searchObjectsByType(String type, String userSuperapp, String userEmail, int size, int page) {
        // Validate user
        UserBoundary user = userService.getUserById(userSuperapp, userEmail)
                .orElseThrow(() -> new CustomBadRequestException("User not found"));

        List<EntityObject> entityObjects;

        if (user.getRole() == UserRole.SUPERAPP_USER) {
            entityObjects = entityObjectRepository.findByType(type, PageRequest.of(page, size, Direction.ASC, "id")).getContent();
        } else if (user.getRole() == UserRole.MINIAPP_USER) {
            entityObjects = entityObjectRepository.findActiveByType(type, PageRequest.of(page, size, Direction.ASC, "id")).getContent();
        } else {
            throw new CustomNotAuthorizedOpperation("User does not have the required permissions");
        }

        return entityObjects.stream().map(objectConverter::toBoundary).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoundaryObject> searchObjectsByAlias(String alias, String userSuperapp, String userEmail, int size, int page) {
        UserBoundary user = userService.getUserById(userSuperapp, userEmail)
                .orElseThrow(() -> new CustomBadRequestException("User not found"));

        List<EntityObject> entityObjects;

        if (user.getRole() == UserRole.SUPERAPP_USER) {
            entityObjects = entityObjectRepository.findByAlias(alias, PageRequest.of(page, size, Direction.ASC, "id")).getContent();
        } else if (user.getRole() == UserRole.MINIAPP_USER) {
            entityObjects = entityObjectRepository.findActiveByAlias(alias, PageRequest.of(page, size, Direction.ASC, "id")).getContent();
        } else {
            throw new CustomNotAuthorizedOpperation("User does not have the required permissions");
        }

        return entityObjects.stream().map(objectConverter::toBoundary).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoundaryObject> searchObjectsByAliasPattern(String pattern, String userSuperapp, String userEmail, int size, int page) {
        UserBoundary user = userService.getUserById(userSuperapp, userEmail)
                .orElseThrow(() -> new CustomBadRequestException("User not found"));

        List<EntityObject> entityObjects;

        if (user.getRole() == UserRole.SUPERAPP_USER) {
            entityObjects = entityObjectRepository.findByAliasPattern(pattern, PageRequest.of(page, size, Direction.ASC, "id")).getContent();
        } else if (user.getRole() == UserRole.MINIAPP_USER) {
            entityObjects = entityObjectRepository.findActiveByAliasPattern(pattern, PageRequest.of(page, size, Direction.ASC, "id")).getContent();
        } else {
            throw new CustomNotAuthorizedOpperation("User does not have the required permissions");
        }

        return entityObjects.stream().map(objectConverter::toBoundary).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoundaryObject> searchObjectsByLocation(double lat, double lng, double distance, String distanceUnits, String userSuperapp, String userEmail, int size, int page) {
        UserBoundary user = userService.getUserById(userSuperapp, userEmail)
                .orElseThrow(() -> new CustomBadRequestException("User not found"));

        List<EntityObject> entityObjects;

        if (user.getRole() == UserRole.SUPERAPP_USER) {
            entityObjects = entityObjectRepository.findByLocation(lat, lng, distance, distanceUnits, PageRequest.of(page, size, Direction.ASC, "id"));
        } else if (user.getRole() == UserRole.MINIAPP_USER) {
            entityObjects = entityObjectRepository.findActiveByLocation(lat, lng, distance, distanceUnits, PageRequest.of(page, size, Direction.ASC, "id"));
        } else {
            throw new CustomNotAuthorizedOpperation("User does not have the required permissions");
        }

        return entityObjects.stream().map(objectConverter::toBoundary).collect(Collectors.toList());
    }

    
    
    
    private void validateUpdatedObject(BoundaryObject boundaryObject) {
        if (boundaryObject == null) {
            throw new CustomBadRequestException("BoundaryObject cannot be null");
        }

        if (boundaryObject.getObjectId() == null) {
            throw new CustomBadRequestException("ObjectId cannot be null");
        }

        if (boundaryObject.getObjectId().getSuperapp() == null || boundaryObject.getObjectId().getSuperapp().trim().isEmpty()) {
            throw new CustomBadRequestException("Superapp within ObjectId cannot be null or empty");
        }

        if (boundaryObject.getObjectId().getId() == null || boundaryObject.getObjectId().getId().trim().isEmpty()) {
            throw new CustomBadRequestException("Id within ObjectId cannot be null or empty");
        }
    }
    
    private void validateCreateObject(BoundaryObject boundaryObject) {
        if (boundaryObject == null) {
            throw new CustomBadRequestException("BoundaryObject cannot be null");
        }

        if (boundaryObject.getType() == null || boundaryObject.getType().trim().isEmpty()) {
            throw new CustomBadRequestException("Type cannot be null or empty");
        }

        if (boundaryObject.getAlias() == null || boundaryObject.getAlias().trim().isEmpty()) {
            throw new CustomBadRequestException("Alias cannot be null or empty");
        }

        if (boundaryObject.getLocation() == null) {
            BoundaryObject.LocationBoundary defaultLocation = new BoundaryObject.LocationBoundary(0, 0);
            boundaryObject.setLocation(defaultLocation);
            //WE know that you asked to put a valid location when an object is created but 
            //we were also told in class that because not all objects have an actual location
            //then we will put a defaultive lat:0 lng:0 in case an object doesnt have the location in the json
            //but we did check that in case he does insert a location that its valid
        } else {
            double lat = boundaryObject.getLocation().getLat();
            double lng = boundaryObject.getLocation().getLng();
            if (lat < -90 || lat > 90) {
                throw new CustomBadRequestException("Invalid latitude value: " + lat);
            }
            if (lng < -180 || lng > 180) {
                throw new CustomBadRequestException("Invalid longitude value: " + lng);
            }
        }

        if (boundaryObject.getCreatedBy() == null) {
            throw new CustomBadRequestException("CreatedBy cannot be null");
        }

        if (boundaryObject.getCreatedBy().getUserId() == null) {
            throw new CustomBadRequestException("UserId within CreatedBy cannot be null");
        }

        String createdByEmail = boundaryObject.getCreatedBy().getUserId().getEmail();
        if (createdByEmail == null || createdByEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email within UserId cannot be null or empty");
        }

        String userSuperapp = boundaryObject.getCreatedBy().getUserId().getSuperapp();
        if (userSuperapp == null || userSuperapp.trim().isEmpty()) {
            throw new CustomBadRequestException("Superapp within UserId cannot be null or empty");
        }

        if (boundaryObject.getObjectDetails() == null) {
            throw new CustomBadRequestException("ObjectDetails cannot be null");
        }

        Optional<UserBoundary> userOpt = userService.getUserById(userSuperapp, createdByEmail);
        if (userOpt.isEmpty()) {
            throw new CustomBadRequestException("User not found: " + createdByEmail);
        }

        UserBoundary user = userOpt.get();
        if (!userSuperapp.equals(superapp)) {
            throw new CustomBadRequestException("Superapp mismatch: user does not belong to this superapp");
        }

        if (user.getRole() == UserRole.ADMIN) {
            throw new CustomNotAuthorizedOpperation("Admins are not authorized to create objects");
        }
    }




 
}
