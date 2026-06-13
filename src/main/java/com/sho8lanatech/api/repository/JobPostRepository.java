package com.sho8lanatech.api.repository;

import com.sho8lanatech.api.model.JobPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostRepository extends JpaRepository<JobPostEntity, Long> {
}
