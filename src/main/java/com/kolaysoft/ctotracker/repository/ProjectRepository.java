package com.kolaysoft.ctotracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kolaysoft.ctotracker.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** Bir proje yoneticisine atanmis projeler (on analiz H-02). */
    List<Project> findByOwnerId(Long ownerId);
}
