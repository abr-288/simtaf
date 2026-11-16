package com.menewa.projects;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByOrderByCreatedAtDesc();
}
