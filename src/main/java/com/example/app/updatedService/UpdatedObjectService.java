package com.example.app.updatedService;

import java.util.List;

import com.example.app.model.BoundaryObject;
import com.example.app.service.ObjectService;

public interface UpdatedObjectService extends ObjectService {
	
	BoundaryObject createObject(BoundaryObject boundaryObject);
	
	void updateObject(String superapp, String id, String userSuperapp, String userEmail, BoundaryObject boundaryObject);

    BoundaryObject getObject(String superapp, String id, String userSuperapp, String userEmail);

	List<BoundaryObject> getAllObjects(String superapp, String email, int size, int page);
	
	List<BoundaryObject> searchObjectsByType(String type, String userSuperapp, String userEmail, int size, int page);
	
    List<BoundaryObject> searchObjectsByAlias(String alias, String userSuperapp, String userEmail, int size, int page);
    
    List<BoundaryObject> searchObjectsByAliasPattern(String pattern, String userSuperapp, String userEmail, int size, int page);
    
    List<BoundaryObject> searchObjectsByLocation(double lat, double lng, double distance, String distanceUnits, String userSuperapp, String userEmail, int size, int page);

}
