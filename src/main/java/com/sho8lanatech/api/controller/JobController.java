package com.sho8lanatech.api.controller;

import com.sho8lanatech.api.dto.JobPost;
import com.sho8lanatech.api.dto.JobRequest;
import com.sho8lanatech.api.dto.JobResponse;
import com.sho8lanatech.api.dto.JobSummaryResponse;
import com.sho8lanatech.api.service.JobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public void createJob(@RequestBody String text) {

        jobService.createJob(text);
    }

    @GetMapping
    public Page<JobSummaryResponse> getAllJobs(
            @RequestParam(required = false) String track,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("publishedAt").descending()
                );

        return jobService.getAllJobs(
                track,
                search,
                pageable
        );
    }

    @GetMapping("/{id}")
    public JobResponse getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @PutMapping("/telegram/{telegramMessageId}")
    public void updateJob(
            @PathVariable Long telegramMessageId,
            @RequestBody String text) {

        jobService.updateJobByTelegramMessageId(
                telegramMessageId,
                text
        );
    }

    @DeleteMapping("/telegram/{telegramMessageId}")
    public void deleteJob(
            @PathVariable Long telegramMessageId) {

        jobService.deleteJobByTelegramMessageId(
                telegramMessageId
        );
    }
}
