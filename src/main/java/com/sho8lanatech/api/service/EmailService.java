package com.sho8lanatech.api.service;

import com.sho8lanatech.api.dto.ContactRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    public void sendEmail(ContactRequest request) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "sho8lana-tech/1.0");

        String body = "{"
                + "\"from\": \"onboarding@resend.dev\","
                + "\"to\": [\"abdalla.fadl.work@gmail.com\"],"
                + "\"subject\": \"" + request.getSubject() + "\","
                + "\"html\": \"<h3>From: " + request.getName() + "</h3>"
                + "<p>Email: " + request.getEmail() + "</p>"
                + "<p>Message: " + request.getMessage() + "</p>\""
                + "}";

        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(url, requestEntity, String.class);
    }
}
