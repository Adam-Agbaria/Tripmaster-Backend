package com.example.app.service;

import com.example.app.model.BoundaryCommand;


public interface CommandService {

    BoundaryCommand createCommand(BoundaryCommand boundaryCommand, String miniAppName);
}
