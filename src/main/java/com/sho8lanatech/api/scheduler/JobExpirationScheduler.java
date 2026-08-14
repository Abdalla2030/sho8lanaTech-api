package com.sho8lanatech.api.scheduler;

import com.sho8lanatech.api.service.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobExpirationScheduler {

    private final JobService jobService;

    public JobExpirationScheduler(JobService jobService) {
        this.jobService = jobService;
    }

    @Scheduled(
            cron = "0 30 17 * * *",
            zone = "Africa/Cairo"
    )
    public void deactivateExpiredJobs() {
        jobService.deactivateExpiredJobs(30);
    }
}
