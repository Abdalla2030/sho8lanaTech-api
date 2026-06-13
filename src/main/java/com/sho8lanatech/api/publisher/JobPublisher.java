package com.sho8lanatech.api.publisher;

import com.sho8lanatech.api.dto.JobPost;

public interface JobPublisher {

    void publish(JobPost jobPost);
}
