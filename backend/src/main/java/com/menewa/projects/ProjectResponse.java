package com.menewa.projects;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(UUID id, String name, String description, Instant createdAt) {
    static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.id(), project.name(), project.description(), project.createdAt());
    }
}
