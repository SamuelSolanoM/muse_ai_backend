package com.muse_ai.logic.external.cma;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class CMAService {

    private static final String BASE_URL = "https://openaccess-api.clevelandart.org/api/artworks";

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> search(String type, String query, String period) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL);

        if (type != null && !type.isBlank()) {
            builder.queryParam("type", type);
        }

        if (query != null && !query.isBlank()) {
            builder.queryParam("q", query);
        }

        if (period != null && !period.isBlank()) {
            builder.queryParam("period", period);
        }

        String url = builder.toUriString();
        System.out.println("URL FINAL → " + url);

        Map response = restTemplate.getForObject(url, Map.class);

        return (List<Map<String, Object>>) response.get("data");
    }

    public Map<String, Object> getArtwork(String id) {

        String url = BASE_URL + "/" + id;

        Map response = restTemplate.getForObject(url, Map.class);

        return (Map<String, Object>) response.get("data");
    }
}
