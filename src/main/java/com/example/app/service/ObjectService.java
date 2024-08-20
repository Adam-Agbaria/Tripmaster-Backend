package com.example.app.service;

import java.util.List;
import java.util.Optional;

import com.example.app.model.BoundaryObject;

public interface ObjectService {
    
    @Deprecated
    Optional<BoundaryObject> getObject(String superappPassed, String id);
    
    @Deprecated
    void updateObject(String superappPassed, String id, BoundaryObject boundaryObject);
  
    @Deprecated
    List<BoundaryObject> getAllObjects(); 
    
    
}
