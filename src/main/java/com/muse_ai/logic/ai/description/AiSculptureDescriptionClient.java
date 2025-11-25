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
import java.util.Objects;
import java.util.Optional;

@Component
public class AiSculptureDescriptionClient {

    private static final Logger log = LoggerFactory.getLogger(AiSculptureDescriptionClient.class);

    private final RestClient restClient;
    private final String model;
    private final String apiKey;

    public AiSculptureDescriptionClient(
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

    public String describeSculpture(String name, List<String> labels, String language) {
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
                                        "content", userPrompt(name, labels)
                                )
                        ),
                        "max_tokens", 200
                ))
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Empty response from OpenAI sculpture description service");
        }

        logUsage(response.usage());

        return Optional.ofNullable(response.choices().getFirst().message().content())
                .map(String::trim)
                .orElseThrow(() -> new IllegalStateException("OpenAI response did not include description content"));
    }

    private void logUsage(Usage usage) {
        if (usage == null) {
            return;
        }
        log.info("OpenAI sculpture description tokens used - prompt: {}, completion: {}, total: {}",
                usage.promptTokens(), usage.completionTokens(), usage.totalTokens());
    }

    private String systemPrompt(String language) {
        return """
                You write concise, evocative museum wall labels for sculptures.
                Respond in the user's language (%s).
                Keep it to 2-3 sentences, focusing on material, form, style, and mood.
                """.formatted(language);
    }

    private String userPrompt(String name, List<String> labels) {
        return """
                Create a description for the sculpture "%s".
                Labels / keywords: %s.
                Avoid inventing details; stay aligned with the labels provided.
                """.formatted(
                Objects.requireNonNullElse(name, ""),
                labels == null ? "" : String.join(", ", labels)
        );
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
