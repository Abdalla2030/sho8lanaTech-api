package com.sho8lanatech.api.repository;

import com.sho8lanatech.api.model.JobPostEntity;
import com.sho8lanatech.api.model.JobPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JobPostRepository extends JpaRepository<JobPostEntity, Long> {

    Optional<JobPostEntity> findByTelegramMessageId(Long telegramMessageId);

    List<JobPostEntity> findByStatusAndPublishedAtBefore(
            JobPostStatus status,
            OffsetDateTime publishedAt
    );

    @Query(
            "SELECT DISTINCT jobPost " +
                    "FROM JobPostEntity jobPost " +
                    "LEFT JOIN jobPost.tracks track " +
                    "WHERE jobPost.status = :status " +
                    "AND (:trackCode IS NULL OR track.code = :trackCode) " +
                    "AND (:search IS NULL OR LOWER(jobPost.formattedText) " +
                    "LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))"
    )
    Page<JobPostEntity> findJobs(
            @Param("status") JobPostStatus status,
            @Param("trackCode") String trackCode,
            @Param("search") String search,
            Pageable pageable
    );

}
