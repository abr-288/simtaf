package com.menewa.projects;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectManagement projects;

    ProjectController(ProjectManagement projects) {
        this.projects = projects;
    }

    @GetMapping
    List<ProjectResponse> listProjects() {
        return projects.listProjects();
    }

    @PostMapping
    ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        var project = projects.createProject(request.name(), request.description());
        return ResponseEntity.created(URI.create("/api/projects/" + project.id())).body(project);
    }
}
