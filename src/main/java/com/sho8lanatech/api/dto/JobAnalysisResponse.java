package com.sho8lanatech.api.dto;

import java.util.List;

public class JobAnalysisResponse {

    private List<String> hashtags;

    private String title;

    private String company;

    private String level;

    private String location;

    private String work_mode;

    private String employment_type;

    private String required_years;

    private String description;

    private List<String> apply;

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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWork_mode() {
        return work_mode;
    }

    public void setWork_mode(String work_mode) {
        this.work_mode = work_mode;
    }

    public String getEmployment_type() {
        return employment_type;
    }

    public void setEmployment_type(String employment_type) {
        this.employment_type = employment_type;
    }

    public String getRequired_years() {
        return required_years;
    }

    public void setRequired_years(String required_years) {
        this.required_years = required_years;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getApply() {
        return apply;
    }

    public void setApply(List<String> apply) {
        this.apply = apply;
    }
}
