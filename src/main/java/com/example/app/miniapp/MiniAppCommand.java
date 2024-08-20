package com.example.app.miniapp;

import com.example.app.model.BoundaryCommand;

public interface MiniAppCommand {
    BoundaryCommand execute(BoundaryCommand boundaryCommand);
}
