package com.muse_ai.logic.ai.translation;

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
public class AiTranslationClient {

    private static final Logger log = LoggerFactory.getLogger(AiTranslationClient.class);

    private final RestClient restClient;
    private final String model;
    private final String apiKey;

    public AiTranslationClient(
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

    public String translateDescription(String originalText, String sourceLanguage, String targetLanguage) {
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
                                        "content", systemPrompt()
                                ),
                                Map.of(
                                        "role", "user",
                                        "content", userPrompt(originalText, sourceLanguage, targetLanguage)
                                )
                        )
                ))
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Empty response from OpenAI translation service");
        }

        logUsage(response.usage());

        String content = Optional.ofNullable(response.choices().getFirst().message().content())
                .orElseThrow(() -> new IllegalStateException("OpenAI response did not include translated content"));
        return content;
    }

    private void logUsage(Usage usage) {
        if (usage == null) {
            return;
        }
        log.info("OpenAI translation tokens used - prompt: {}, completion: {}, total: {}",
                usage.promptTokens(), usage.completionTokens(), usage.totalTokens());
    }

    private String systemPrompt() {
        return "You translate artwork descriptions. Preserve the user's formatting, line breaks, punctuation, and markdown.";
    }

    private String userPrompt(String originalText, String sourceLanguage, String targetLanguage) {
        return """
                Translate the following artwork description from %s to %s.
                Keep the same formatting, including bullet points, paragraphs, and spacing.
                Return only the translated text with no explanations or notes.

                %s
                """.formatted(
                Objects.requireNonNullElse(sourceLanguage, ""),
                Objects.requireNonNullElse(targetLanguage, ""),
                Objects.requireNonNullElse(originalText, "")
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
