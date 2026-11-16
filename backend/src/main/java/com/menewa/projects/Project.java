package com.menewa.projects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Project() {}

    Project(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.createdAt = Instant.now();
    }

    UUID id() {
        return id;
    }

    String name() {
        return name;
    }

    String description() {
        return description;
    }

    Instant createdAt() {
        return createdAt;
    }
}
