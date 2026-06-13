package com.sho8lanatech.api.dto;

import java.util.List;

public class JobPost {

    private String originalText;

    private String analyzedText;

    private List<String> tags;

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getAnalyzedText() {
        return analyzedText;
    }

    public void setAnalyzedText(String analyzedText) {
        this.analyzedText = analyzedText;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
