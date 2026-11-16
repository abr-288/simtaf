package com.menewa.projects;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectManagement {
    private final ProjectRepository projects;

    ProjectManagement(ProjectRepository projects) {
        this.projects = projects;
    }

    List<ProjectResponse> listProjects() {
        return projects.findAllByOrderByCreatedAtDesc().stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional
    ProjectResponse createProject(String name, String description) {
        var project = projects.save(new Project(name.trim(), normalize(description)));
        return ProjectResponse.from(project);
    }

    private String normalize(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
