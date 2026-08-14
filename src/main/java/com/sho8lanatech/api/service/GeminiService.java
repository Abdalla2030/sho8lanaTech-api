package com.sho8lanatech.api.service;

import com.sho8lanatech.api.dto.JobPost;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient =
            WebClient.builder()
                    .clientConnector(
                            new ReactorClientHttpConnector(
                                    HttpClient.create().secure(
                                            ssl -> {
                                                try {
                                                    ssl.sslContext(
                                                            SslContextBuilder.forClient()
                                                                    .trustManager(
                                                                            InsecureTrustManagerFactory.INSTANCE
                                                                    )
                                                                    .build()
                                                    );
                                                } catch (Exception ex) {
                                                    throw new RuntimeException(ex);
                                                }
                                            }
                                    )
                            )
                    )
                    .build();

    public JobPost analyzeJob(String text) {

        String prompt =
                loadPrompt()
                        + "\n\nAnalyze the following job post:\n\n"
                        + text;

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        content.put("parts", List.of(textPart));

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));

        Map response;

        try {
            response = webClient.post()
                    .uri(
                            "https://generativelanguage.googleapis.com/v1beta/"
                                    + "models/gemini-3.1-flash-lite:generateContent"
                    )
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException(
                    "Gemini error: "
                            + ex.getResponseBodyAsString(),
                    ex
            );
        }

        List candidates = (List) response.get("candidates");

        Map firstCandidate = (Map) candidates.get(0);

        Map responseContent =
                (Map) firstCandidate.get("content");

        List parts =
                (List) responseContent.get("parts");

        Map firstPart =
                (Map) parts.get(0);

        String analyzedText =
                firstPart.get("text").toString();

        JobPost jobPost = new JobPost();
        jobPost.setOriginalText(text);
        jobPost.setAnalyzedText(analyzedText);

        return jobPost;
    }

    private String loadPrompt() {

        try {
            ClassPathResource resource =
                    new ClassPathResource(
                            "prompts/job-analysis-prompt.txt"
                    );

            byte[] bytes =
                    resource.getInputStream().readAllBytes();

            return new String(
                    bytes,
                    StandardCharsets.UTF_8
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
