package com.example.app.miniapp.commands;

import com.example.app.miniapp.MiniAppCommand;
import com.example.app.model.BoundaryCommand;
import com.example.app.model.EntityObject;
import com.example.app.repository.EntityObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component("findObjectsByCreatorEmailAndType")
public class FindObjectsByCreatorEmailAndTypeCommand implements MiniAppCommand {

    private final EntityObjectRepository entityObjectRepository;

    @Autowired
    public FindObjectsByCreatorEmailAndTypeCommand(EntityObjectRepository entityObjectRepository) {
        this.entityObjectRepository = entityObjectRepository;
    }

    @Override
    public BoundaryCommand execute(BoundaryCommand boundaryCommand) {
        String email = (String) boundaryCommand.getCommandAttributes().get("email");
        String type = (String) boundaryCommand.getCommandAttributes().get("type");
        Integer page = (Integer) boundaryCommand.getCommandAttributes().getOrDefault("page", 0);
        Integer size = (Integer) boundaryCommand.getCommandAttributes().getOrDefault("size", 10);

        if (email == null || email.isEmpty()) {
            boundaryCommand.getCommandAttributes().put("error", "Email is missing or empty");
            return boundaryCommand;
        }
        if (type == null || type.isEmpty()) {
            boundaryCommand.getCommandAttributes().put("error", "Type is missing or empty");
            return boundaryCommand;
        }

        if (page < 0) {
            boundaryCommand.getCommandAttributes().put("error", "Page number cannot be negative");
            return boundaryCommand;
        }
        if (size <= 0) {
            boundaryCommand.getCommandAttributes().put("error", "Page size must be greater than zero");
            return boundaryCommand;
        }

        Page<EntityObject> objectsPage;
        try {
            objectsPage = entityObjectRepository.findByCreatorEmailAndType(email, type, PageRequest.of(page, size));
        } catch (Exception e) {
            boundaryCommand.getCommandAttributes().put("error", "Failed to fetch objects: " + e.getMessage());
            return boundaryCommand;
        }

        boundaryCommand.getCommandAttributes().put("results", objectsPage.getContent());
        boundaryCommand.getCommandAttributes().put("totalPages", objectsPage.getTotalPages());
        boundaryCommand.getCommandAttributes().put("totalElements", objectsPage.getTotalElements());

        return boundaryCommand;
    }
}
