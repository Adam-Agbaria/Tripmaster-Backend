package com.example.app.service;

import com.example.app.model.BoundaryCommand;
import com.example.app.model.UserBoundary;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface AdminService {

    @Deprecated 
    List<BoundaryCommand> exportAllCommandsHistory();

    @Deprecated 
    List<BoundaryCommand> exportCommandsHistoryByMiniApp(String miniAppName);

    @Deprecated 
    void deleteAllCommandsHistory();

    @Deprecated 
    List<UserBoundary> getAllUsers();

    @Deprecated 
    void deleteAllUsers();

    @Deprecated 
    void deleteAllObjects();
    

}
