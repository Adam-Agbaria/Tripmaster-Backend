package com.example.app.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.app.model.BoundaryCommand;
import com.example.app.model.EntityCommand;

public interface EntityCommandRepository extends JpaRepository<EntityCommand, String> {

    Page<EntityCommand> findByMiniAppName(String miniAppName, Pageable pageable);
}
