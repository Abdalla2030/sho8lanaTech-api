package com.sho8lanatech.api.controller;

import com.sho8lanatech.api.dto.JobPost;
import com.sho8lanatech.api.dto.JobRequest;
import com.sho8lanatech.api.dto.JobResponse;
import com.sho8lanatech.api.service.JobService;
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
    public List<JobResponse> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/{id}")
    public JobResponse getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }
}
