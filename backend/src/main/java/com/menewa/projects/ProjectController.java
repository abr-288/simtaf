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
import org.springframework.web.bind.annotation.RequestHeader;
import com.menewa.identity.IdentityAccess;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectManagement projects;
    private final IdentityAccess identity;

    ProjectController(ProjectManagement projects, IdentityAccess identity) {
        this.projects = projects;
        this.identity = identity;
    }

    @GetMapping
    List<ProjectResponse> listProjects(@RequestHeader(value = "Authorization", required = false) String authorization) {
        identity.requireUser(authorization);
        return projects.listProjects();
    }

    @PostMapping
    ResponseEntity<ProjectResponse> createProject(@RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CreateProjectRequest request) {
        identity.requireUser(authorization);
        var project = projects.createProject(request.name(), request.description());
        return ResponseEntity.created(URI.create("/api/projects/" + project.id())).body(project);
    }
}
