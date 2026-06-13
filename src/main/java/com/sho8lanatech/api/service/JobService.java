package com.sho8lanatech.api.service;

import com.sho8lanatech.api.dto.JobPost;
import com.sho8lanatech.api.dto.JobRequest;
import com.sho8lanatech.api.dto.JobResponse;
import com.sho8lanatech.api.formatter.JobPostParser;
import com.sho8lanatech.api.model.JobPostEntity;
import com.sho8lanatech.api.publisher.JobPublisher;
import com.sho8lanatech.api.repository.JobPostRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobService {

    private final CohereService cohereService;

    private final List<JobPublisher> publishers;

    private final JobPostRepository jobPostRepository;

    public JobService(CohereService CohereService,
                      List<JobPublisher> publishers,
                      JobPostRepository jobPostRepository) {

        this.cohereService = CohereService;
        this.publishers = publishers;
        this.jobPostRepository = jobPostRepository;
    }

    @Async
    public void  createJob(String text) {

        JobPost jobPost =
                cohereService.analyzeJob(text);

        for (JobPublisher publisher : publishers) {
            publisher.publish(jobPost);
        }

    }

    public List<JobResponse> getAllJobs() {

        List<JobPostEntity> jobPostEntities = jobPostRepository.findAll();

        List<JobResponse> jobResponses = new ArrayList<>();

        for (JobPostEntity jobPostEntity : jobPostEntities) {
            jobResponses.add(mapToJobResponse(jobPostEntity));
        }

        return jobResponses;
    }

    public JobResponse getJobById(Long id) {

        JobPostEntity jobPostEntity = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        return mapToJobResponse(jobPostEntity);
    }

    private JobResponse mapToJobResponse(JobPostEntity jobPostEntity) {

        JobResponse jobResponse = JobPostParser.parse(jobPostEntity.getFormattedText());

        jobResponse.setId(jobPostEntity.getId());
        jobResponse.setStatus(jobPostEntity.getStatus());
        jobResponse.setTelegramUrl(jobPostEntity.getTelegramUrl());
        jobResponse.setTelegramMessageId(jobPostEntity.getTelegramMessageId());
        jobResponse.setCreatedAt(jobPostEntity.getCreatedAt());
        jobResponse.setPublishedAt(jobPostEntity.getPublishedAt());

        //jobResponse.setDescription(jobPostEntity.getFormattedText());

        return jobResponse;
    }
}
