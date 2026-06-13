package com.sho8lanatech.api.service;

import com.sho8lanatech.api.dto.JobPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CohereService {

    @Value("${cohere.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.create();

    public JobPost analyzeJob(String text) {

        String prompt = loadPrompt() + "\n\nAnalyze the following job post:\n\n" + text;

        Map<String, Object> body = new HashMap<>();

        body.put("model", "command-a-03-2025");

        List<Map<String, String>> messages = List.of(
                createMessage("user", prompt)
        );

        body.put("messages", messages);

        Map response = webClient.post()
                .uri("https://api.cohere.com/v2/chat")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map message = (Map) response.get("message");

        List content = (List) message.get("content");

        Map first = (Map) content.get(0);

        String analyzedText = first.get("text").toString();

        JobPost jobPost = new JobPost();
        jobPost.setOriginalText(text);
        jobPost.setAnalyzedText(analyzedText);

        return jobPost;


    }

    private Map<String, String> createMessage(String role, String content) {

        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);

        return message;
    }

    private String loadPrompt() {

        try {

            ClassPathResource resource =
                    new ClassPathResource("prompts/job-analysis-prompt.txt");

            byte[] bytes = resource.getInputStream().readAllBytes();

            return new String(bytes, StandardCharsets.UTF_8);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
