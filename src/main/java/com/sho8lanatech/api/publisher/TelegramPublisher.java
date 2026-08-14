package com.sho8lanatech.api.publisher;

import com.sho8lanatech.api.dto.JobPost;
import com.sho8lanatech.api.model.JobPostEntity;
import com.sho8lanatech.api.model.JobPostStatus;
import com.sho8lanatech.api.model.JobPostTelegramMessageEntity;
import com.sho8lanatech.api.model.TrackEntity;
import com.sho8lanatech.api.repository.JobPostRepository;
import com.sho8lanatech.api.repository.JobPostTelegramMessageRepository;
import com.sho8lanatech.api.repository.TrackRepository;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private final JobPostTelegramMessageRepository telegramMessageRepository;

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

    public TelegramPublisher(
            JobPostRepository jobPostRepository,
            TrackRepository trackRepository,
            JobPostTelegramMessageRepository telegramMessageRepository) {

        this.jobPostRepository = jobPostRepository;
        this.trackRepository = trackRepository;
        this.telegramMessageRepository = telegramMessageRepository;
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
        jobPostEntity.setTelegramUrl(
                "https://t.me/sho8lanaTech/" + messageId
        );
        jobPostEntity.setStatus(JobPostStatus.PUBLISHED);
        jobPostEntity.setCreatedAt(OffsetDateTime.now());
        jobPostEntity.setPublishedAt(OffsetDateTime.now());

        Set<TrackEntity> tracks =
                resolveTracks(jobPost.getAnalyzedText());

        jobPostEntity.setTracks(tracks);

        jobPostRepository.save(jobPostEntity);

        sendToTopics(
                jobPostEntity,
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
        body.put("parse_mode", "HTML");

        Map response;

        try {
            response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException(
                    "Telegram error: " + ex.getResponseBodyAsString(),
                    ex
            );
        }

        Map result = (Map) response.get("result");

        return (Integer) result.get("message_id");
    }

    private void sendToTopics(
            JobPostEntity jobPostEntity,
            String text) {

        String firstLine = text.split("\n")[0];

        Set<Integer> sentTopicIds = new HashSet<>();

        for (Map.Entry<String, Integer> entry : TOPICS.entrySet()) {

            String tag = entry.getKey();
            Integer topicId = entry.getValue();

            if (!firstLine.contains(tag) || !sentTopicIds.add(topicId)) {
                continue;
            }

            Integer topicMessageId =
                    sendToTopic(text, topicId);

            JobPostTelegramMessageEntity telegramMessage =
                    new JobPostTelegramMessageEntity();

            telegramMessage.setJobPost(jobPostEntity);
            telegramMessage.setTopicId(topicId);
            telegramMessage.setTelegramMessageId(
                    Long.valueOf(topicMessageId)
            );

            telegramMessageRepository.save(telegramMessage);
        }
    }

    public Integer sendToTopic(
            String text,
            Integer topicId) {

        String url =
                "https://api.telegram.org/bot"
                        + botToken
                        + "/sendMessage";

        Map<String, Object> body = new HashMap<>();

        body.put("chat_id", communityId);
        body.put("message_thread_id", topicId);
        body.put("text", text);
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

    public Set<TrackEntity> resolveTracks(String text) {

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
        if (firstLine.contains("#INT") || firstLine.contains("#GEN")) {
            trackRepository.findByCode("INT_GEN").ifPresent(tracks::add);
        }
        if (firstLine.contains("#TA") || firstLine.contains("#INS")) {
            trackRepository.findByCode("TA_INS").ifPresent(tracks::add);
        }

        return tracks;
    }

    private void addTrackIfExists(
            Set<TrackEntity> tracks,
            String firstLine,
            String hashtag,
            String code) {

        if (!firstLine.contains(hashtag)) {
            return;
        }

        trackRepository.findByCode(code)
                .ifPresent(tracks::add);
    }

    public void editChannelMessage(Long messageId, String text) {
        editMessage(channelId, messageId, text);
    }

    public void editTopicMessage(Long messageId, String text) {
        editMessage(communityId, messageId, text);
    }

    private void editMessage(String chatId, Long messageId, String text) {

        String url =
                "https://api.telegram.org/bot"
                        + botToken
                        + "/editMessageText";

        Map<String, Object> body = new HashMap<>();

        body.put("chat_id", chatId);
        body.put("message_id", messageId);
        body.put("text", text);
        body.put("parse_mode", "HTML");

        webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public Set<Integer> resolveTopicIds(String text) {

        Set<Integer> topicIds = new LinkedHashSet<>();

        String firstLine = text.split("\n")[0];

        for (Map.Entry<String, Integer> entry : TOPICS.entrySet()) {

            if (firstLine.contains(entry.getKey())) {
                topicIds.add(entry.getValue());
            }
        }

        return topicIds;
    }


    public void deleteChannelMessage(Long messageId) {
        deleteMessage(channelId, messageId);
    }

    public void deleteTopicMessage(Long messageId) {
        deleteMessage(communityId, messageId);
    }

    private void deleteMessage(String chatId, Long messageId) {

        String url =
                "https://api.telegram.org/bot"
                        + botToken
                        + "/deleteMessage";

        Map<String, Object> body = new HashMap<>();

        body.put("chat_id", chatId);
        body.put("message_id", messageId);

        webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(
                        WebClientResponseException.class,
                        ex -> Mono.empty()
                )
                .block();
    }


}
