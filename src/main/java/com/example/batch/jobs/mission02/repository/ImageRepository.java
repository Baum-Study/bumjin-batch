package com.example.batch.jobs.mission02.repository;

import com.example.batch.jobs.mission02.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
