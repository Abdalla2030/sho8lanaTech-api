package com.sho8lanatech.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_post_telegram_message")
public class JobPostTelegramMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPostEntity jobPost;

    @Column(name = "topic_id", nullable = false)
    private Integer topicId;

    @Column(name = "telegram_message_id", nullable = false)
    private Long telegramMessageId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobPostEntity getJobPost() {
        return jobPost;
    }

    public void setJobPost(JobPostEntity jobPost) {
        this.jobPost = jobPost;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }

    public Long getTelegramMessageId() {
        return telegramMessageId;
    }

    public void setTelegramMessageId(Long telegramMessageId) {
        this.telegramMessageId = telegramMessageId;
    }
}
