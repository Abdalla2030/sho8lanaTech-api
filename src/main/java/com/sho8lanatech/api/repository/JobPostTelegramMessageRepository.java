package com.sho8lanatech.api.repository;

import com.sho8lanatech.api.model.JobPostTelegramMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostTelegramMessageRepository
        extends JpaRepository<JobPostTelegramMessageEntity, Long> {

    List<JobPostTelegramMessageEntity> findByJobPostId(Long jobPostId);

    void deleteByJobPostId(Long jobPostId);
}
