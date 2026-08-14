package com.sho8lanatech.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class JobSummaryResponse {

    private Long id;
    private List<String> hashtags;
    private String title;
    private String company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWorkMode() {
        return workMode;
    }

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public String getRequiredYears() {
        return requiredYears;
    }

    public void setRequiredYears(String requiredYears) {
        this.requiredYears = requiredYears;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    private String location;
    private String workMode;
    private String requiredYears;
    private OffsetDateTime publishedAt;
}
