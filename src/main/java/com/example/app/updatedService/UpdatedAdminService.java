package com.example.app.updatedService;

import java.util.List;

import com.example.app.model.BoundaryCommand;
import com.example.app.model.UserBoundary;
import com.example.app.service.AdminService;

public interface UpdatedAdminService extends AdminService{
	
	void deleteAllUsers(String userSuperapp, String userEmail);

    void deleteAllObjects(String userSuperapp, String userEmail);

    void deleteAllCommands(String userSuperapp, String userEmail);

    List<UserBoundary> getAllUsers(String userSuperapp, String userEmail, int size, int page);

    List<BoundaryCommand> exportAllCommandsHistory(String userSuperapp, String userEmail, int size, int page);

    List<BoundaryCommand> exportCommandsHistoryByMiniApp(String miniAppName, String userSuperapp, String userEmail, int size, int page);

}
