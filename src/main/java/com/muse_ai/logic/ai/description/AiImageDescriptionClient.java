package com.muse_ai.logic.ai.description;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AiImageDescriptionClient {

    private static final Logger log = LoggerFactory.getLogger(AiImageDescriptionClient.class);

    private final RestClient restClient;
    private final String model;
    private final String apiKey;

    public AiImageDescriptionClient(
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model,
            @Value("${openai.base-url:https://api.openai.com/v1}") String baseUrl
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String describeImage(String imageBase64, String language) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }

        ChatCompletionResponse response = restClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of(
                                        "role", "system",
                                        "content", systemPrompt(language)
                                ),
                                Map.of(
                                        "role", "user",
                                        "content", List.of(
                                                Map.of("type", "text", "text", userPrompt(language)),
                                                Map.of("type", "image_url", "image_url", Map.of("url", toDataUrl(imageBase64)))
                                        )
                                )
                        ),
                        "max_tokens", 300
                ))
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Empty response from OpenAI description service");
        }

        logUsage(response.usage());

        return Optional.ofNullable(response.choices().getFirst().message().content())
                .map(String::trim)
                .orElseThrow(() -> new IllegalStateException("OpenAI response did not include description content"));
    }

    private String toDataUrl(String imageBase64) {
        String trimmed = imageBase64 == null ? "" : imageBase64.trim();
        if (trimmed.startsWith("data:image/")) {
            return trimmed;
        }
        return "data:image/jpeg;base64," + trimmed;
    }

    private void logUsage(Usage usage) {
        if (usage == null) {
            return;
        }
        log.info("OpenAI image description tokens used - prompt: {}, completion: {}, total: {}",
                usage.promptTokens(), usage.completionTokens(), usage.totalTokens());
    }

    private String systemPrompt(String language) {
        return """
                You are an art expert that creates concise descriptions of artworks.
                Respond in the user's language (%s).
                Do not mention the fact that you are looking at an image or speculate beyond what is visible.
                """.formatted(language);
    }

    private String userPrompt(String language) {
        return "Provide a short, vivid description in " + language + " that covers subject, medium, style, and mood.";
    }

    record ChatCompletionResponse(List<Choice> choices, Usage usage) {
    }

    record Choice(Message message) {
    }

    record Message(String role, String content) {
    }

    record Usage(
            @JsonProperty("prompt_tokens") Integer promptTokens,
            @JsonProperty("completion_tokens") Integer completionTokens,
            @JsonProperty("total_tokens") Integer totalTokens
    ) {
    }
}
