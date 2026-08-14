package com.sho8lanatech.api.service;

import com.sho8lanatech.api.dto.JobPost;
import com.sho8lanatech.api.dto.JobResponse;
import com.sho8lanatech.api.dto.JobSummaryResponse;
import com.sho8lanatech.api.formatter.JobPostParser;
import com.sho8lanatech.api.model.JobPostEntity;
import com.sho8lanatech.api.model.JobPostStatus;
import com.sho8lanatech.api.model.JobPostTelegramMessageEntity;
import com.sho8lanatech.api.publisher.JobPublisher;
import com.sho8lanatech.api.publisher.TelegramPublisher;
import com.sho8lanatech.api.repository.JobPostRepository;
import com.sho8lanatech.api.repository.JobPostTelegramMessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JobService {

    private final GeminiService cohereService;

    private final List<JobPublisher> publishers;

    private final JobPostRepository jobPostRepository;

    private final TelegramPublisher telegramPublisher;

    private final JobPostTelegramMessageRepository telegramMessageRepository;

    public JobService(
            GeminiService cohereService,
            List<JobPublisher> publishers,
            JobPostRepository jobPostRepository,
            TelegramPublisher telegramPublisher,
            JobPostTelegramMessageRepository telegramMessageRepository) {

        this.cohereService = cohereService;
        this.publishers = publishers;
        this.jobPostRepository = jobPostRepository;
        this.telegramPublisher = telegramPublisher;
        this.telegramMessageRepository = telegramMessageRepository;
    }

    @Async
    public void createJob(String text) {

        JobPost jobPost =
                cohereService.analyzeJob(text);

        for (JobPublisher publisher : publishers) {
            publisher.publish(jobPost);
        }
    }

    public void updateJobByTelegramMessageId(
            Long telegramMessageId,
            String text) {

        JobPostEntity jobPostEntity =
                jobPostRepository.findByTelegramMessageId(telegramMessageId)
                        .orElseThrow(() -> new RuntimeException("Job not found"));

        telegramPublisher.editChannelMessage(
                jobPostEntity.getTelegramMessageId(),
                text
        );

        List<JobPostTelegramMessageEntity> oldTopicMessages =
                telegramMessageRepository.findByJobPostId(
                        jobPostEntity.getId()
                );

        Set<Integer> topicsToAdd =
                new HashSet<>(
                        telegramPublisher.resolveTopicIds(text)
                );

        for (JobPostTelegramMessageEntity topicMessage : oldTopicMessages) {

            Integer topicId = topicMessage.getTopicId();

            if (topicsToAdd.contains(topicId)) {

                telegramPublisher.editTopicMessage(
                        topicMessage.getTelegramMessageId(),
                        text
                );

                topicsToAdd.remove(topicId);

            } else {

                telegramPublisher.deleteTopicMessage(
                        topicMessage.getTelegramMessageId()
                );

                telegramMessageRepository.delete(topicMessage);
            }
        }

        for (Integer topicId : topicsToAdd) {

            Integer newMessageId =
                    telegramPublisher.sendToTopic(text, topicId);

            JobPostTelegramMessageEntity newTopicMessage =
                    new JobPostTelegramMessageEntity();

            newTopicMessage.setJobPost(jobPostEntity);
            newTopicMessage.setTopicId(topicId);
            newTopicMessage.setTelegramMessageId(
                    Long.valueOf(newMessageId)
            );

            telegramMessageRepository.save(newTopicMessage);
        }

        jobPostEntity.setFormattedText(text);
        jobPostEntity.setTracks(
                telegramPublisher.resolveTracks(text)
        );

        jobPostRepository.save(jobPostEntity);
    }

    public Page<JobSummaryResponse> getAllJobs(
            String trackCode,
            String search,
            Pageable pageable) {

        return jobPostRepository.findJobs(
                JobPostStatus.PUBLISHED,
                trackCode,
                search,
                pageable
        ).map(this::mapToJobSummaryResponse);
    }

    private JobSummaryResponse mapToJobSummaryResponse(
            JobPostEntity jobPostEntity) {

        JobResponse jobResponse =
                JobPostParser.parse(
                        jobPostEntity.getFormattedText()
                );

        JobSummaryResponse summary =
                new JobSummaryResponse();

        summary.setId(jobPostEntity.getId());
        summary.setHashtags(jobResponse.getHashtags());
        summary.setTitle(jobResponse.getTitle());
        summary.setCompany(jobResponse.getCompany());
        summary.setLocation(jobResponse.getLocation());
        summary.setWorkMode(jobResponse.getWorkMode());
        summary.setRequiredYears(jobResponse.getRequiredYears());
        summary.setPublishedAt(jobPostEntity.getPublishedAt());

        return summary;
    }

    public JobResponse getJobById(Long id) {

        JobPostEntity jobPostEntity =
                jobPostRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Job not found"));

        return mapToJobResponse(jobPostEntity);
    }

    private JobResponse mapToJobResponse(
            JobPostEntity jobPostEntity) {

        JobResponse jobResponse =
                JobPostParser.parse(
                        jobPostEntity.getFormattedText()
                );

        jobResponse.setId(jobPostEntity.getId());
        jobResponse.setStatus(jobPostEntity.getStatus().name());
        jobResponse.setTelegramUrl(jobPostEntity.getTelegramUrl());
        jobResponse.setTelegramMessageId(
                jobPostEntity.getTelegramMessageId()
        );
        jobResponse.setCreatedAt(jobPostEntity.getCreatedAt());
        jobResponse.setPublishedAt(jobPostEntity.getPublishedAt());

        return jobResponse;
    }


    @Transactional
    public void deleteJobByTelegramMessageId(Long telegramMessageId) {

        JobPostEntity jobPostEntity =
                jobPostRepository.findByTelegramMessageId(telegramMessageId)
                        .orElseThrow(() -> new RuntimeException("Job not found"));

        telegramPublisher.deleteChannelMessage(
                jobPostEntity.getTelegramMessageId()
        );

        List<JobPostTelegramMessageEntity> topicMessages =
                telegramMessageRepository.findByJobPostId(
                        jobPostEntity.getId()
                );

        for (JobPostTelegramMessageEntity topicMessage : topicMessages) {
            telegramPublisher.deleteTopicMessage(
                    topicMessage.getTelegramMessageId()
            );
        }

        telegramMessageRepository.deleteByJobPostId(
                jobPostEntity.getId()
        );

        jobPostRepository.delete(jobPostEntity);
    }


    @Transactional
    public void deactivateExpiredJobs(int expirationDays) {

        OffsetDateTime expirationDate =
                OffsetDateTime.now().minusDays(expirationDays);

        List<JobPostEntity> expiredJobs =
                jobPostRepository.findByStatusAndPublishedAtBefore(
                        JobPostStatus.PUBLISHED,
                        expirationDate
                );

        for (JobPostEntity jobPostEntity : expiredJobs) {
            jobPostEntity.setStatus(JobPostStatus.INACTIVE);
        }
    }
}
