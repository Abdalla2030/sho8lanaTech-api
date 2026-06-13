package com.sho8lanatech.api.publisher;

import com.sho8lanatech.api.dto.JobPost;
import com.sho8lanatech.api.model.JobPostEntity;
import com.sho8lanatech.api.model.TrackEntity;
import com.sho8lanatech.api.repository.JobPostRepository;
import com.sho8lanatech.api.repository.TrackRepository;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class TelegramPublisher implements JobPublisher {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.channel.id}")
    private String channelId;

    @Value("${telegram.community.id}")
    private String communityId;

    //private final WebClient webClient = WebClient.create();
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

    private static final Map<String, Integer> TOPICS = new LinkedHashMap<>();

    private final JobPostRepository jobPostRepository;
    private final TrackRepository trackRepository;

    static {

        TOPICS.put("#BE", 27);
        TOPICS.put("#FE", 29);
        TOPICS.put("#MOB", 7);
        TOPICS.put("#IT", 13);
        TOPICS.put("#SEC", 25);
        TOPICS.put("#AI", 2);
        TOPICS.put("#DVC", 23);
        TOPICS.put("#EMB", 9);
        TOPICS.put("#QA", 19);
        TOPICS.put("#DSN", 21);
        TOPICS.put("#INT", 138);
        TOPICS.put("#GEN", 138);
        TOPICS.put("#TA", 11);
        TOPICS.put("#INS", 11);
    }

    public TelegramPublisher(JobPostRepository jobPostRepository, TrackRepository trackRepository) {
        this.jobPostRepository = jobPostRepository;
        this.trackRepository = trackRepository;
    }

    @Override
    public void publish(JobPost jobPost) {

        Integer messageId = sendToChannel(jobPost.getAnalyzedText());

        if (messageId == null) {
            return;
        }


        JobPostEntity jobPostEntity = new JobPostEntity();
        jobPostEntity.setOriginalText(jobPost.getOriginalText());
        jobPostEntity.setFormattedText(jobPost.getAnalyzedText());
        jobPostEntity.setTelegramMessageId(Long.valueOf(messageId));
        jobPostEntity.setTelegramUrl("https://t.me/sho8lanaTech/" +messageId);
        jobPostEntity.setStatus("PUBLISHED");
        jobPostEntity.setCreatedAt(OffsetDateTime.now());
        jobPostEntity.setPublishedAt(OffsetDateTime.now());

        Set<TrackEntity> tracks = resolveTracks(jobPost.getAnalyzedText());
        jobPostEntity.setTracks(tracks);

        jobPostRepository.save(jobPostEntity);

        forwardToTopics(
                messageId,
                jobPost.getAnalyzedText()
        );
    }

    private Integer sendToChannel(String text) {

        String url =
                "https://api.telegram.org/bot"
                        + botToken
                        + "/sendMessage";

        Map<String, Object> body = new HashMap<>();

        body.put("chat_id", channelId);
        body.put("text", text);
        //body.put("parse_mode", "Markdown");
        body.put("parse_mode", "HTML");

        Map response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map result = (Map) response.get("result");

        return (Integer) result.get("message_id");
    }

    private void forwardToTopics(Integer messageId, String text) {

        String firstLine = text.split("\n")[0];

        for (Map.Entry<String, Integer> entry : TOPICS.entrySet()) {

            String tag = entry.getKey();
            Integer topicId = entry.getValue();

            if (!firstLine.contains(tag)) {
                continue;
            }

            forwardMessage(messageId, topicId);
        }
    }

    private void forwardMessage(Integer messageId, Integer topicId) {

        String url =
                "https://api.telegram.org/bot"
                        + botToken
                        + "/forwardMessage";

        Map<String, Object> body = new HashMap<>();

        body.put("chat_id", communityId);
        body.put("from_chat_id", channelId);
        body.put("message_id", messageId);
        body.put("message_thread_id", topicId);

        webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private Set<TrackEntity> resolveTracks(String text) {
        Set<TrackEntity> tracks = new HashSet<>();

        String firstLine = text.split("\n")[0];

        addTrackIfExists(tracks, firstLine, "#BE", "BE");
        addTrackIfExists(tracks, firstLine, "#FE", "FE");
        addTrackIfExists(tracks, firstLine, "#MOB", "MOB");
        addTrackIfExists(tracks, firstLine, "#IT", "IT");
        addTrackIfExists(tracks, firstLine, "#SEC", "SEC");
        addTrackIfExists(tracks, firstLine, "#AI", "AI");
        addTrackIfExists(tracks, firstLine, "#DVC", "DVC");
        addTrackIfExists(tracks, firstLine, "#QA", "QA");
        addTrackIfExists(tracks, firstLine, "#DSN", "DSN");
        addTrackIfExists(tracks, firstLine, "#EMB", "EMB");
        addTrackIfExists(tracks, firstLine, "#INT", "INT_GEN");
        addTrackIfExists(tracks, firstLine, "#GEN", "INT_GEN");
        addTrackIfExists(tracks, firstLine, "#TA", "TA_INS");
        addTrackIfExists(tracks, firstLine, "#INS", "TA_INS");

        return tracks;
    }

    private void addTrackIfExists(Set<TrackEntity> tracks, String firstLine, String hashtag, String code) {
        if (!firstLine.contains(hashtag)) {
            return;
        }

        trackRepository.findByCode(code).ifPresent(tracks::add);
    }



}
