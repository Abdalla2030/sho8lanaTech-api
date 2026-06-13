package com.sho8lanatech.api.publisher;

import com.sho8lanatech.api.dto.JobPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

@Component
public class UpplyPublisher implements JobPublisher {

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


    private String upplyToken;


    @Value("${upply.email}")
    private String upplyEmail;

    @Value("${upply.password}")
    private String upplyPassword;

    @Override
    public void publish(JobPost jobPost) {
        Map<String, Object> body = new HashMap<>();
        body.put("descriptionText", jobPost.getOriginalText());

        sendPostToUpply(body);
    }

    private void sendPostToUpply(Map<String, Object> body) {
        if (upplyToken == null) {
            upplyToken = getUpplyToken();
        }

        try {
            importJob(body);
        } catch (Exception ex) {
            upplyToken = getUpplyToken();
            importJob(body);
        }
    }

    private void importJob(Map<String, Object> body) {
        webClient.post()
                .uri("https://api.upply.tech/api/v1/jobs/import")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + upplyToken)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getUpplyToken() {
        Map<String, Object> body = new HashMap<>();
        body.put("email", upplyEmail);
        body.put("password", upplyPassword);

        Map response = webClient.post()
                .uri("https://www.upply.tech/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return (String) response.get("token");
    }
}
