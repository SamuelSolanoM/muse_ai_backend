package com.muse_ai.rest.sculpture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muse_ai.logic.entity.sculpture.Sculpture;
import com.muse_ai.logic.entity.sculpture.SculptureRepository;
import com.muse_ai.rest.sculpture.dto.SculptureRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class SculptureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SculptureRepository sculptureRepository;

    @WithMockUser
    @Test
    void shouldCreateSculpture() throws Exception {
        SculptureRequest request = new SculptureRequest(
                "Test Sculpture",
                "{\"hello\":true}",
                "{\"units\":\"cm\"}",
                List.of("angular", "scene"),
                null
        );

        mockMvc.perform(post("/api/sculptures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Sculpture"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.sceneJson").value("{\"hello\":true}"));
    }

    @WithMockUser
    @Test
    void shouldRejectOversizedSceneJson() throws Exception {
        int payloadSize = 5 * 1024 * 1024 + 1;
        byte[] bytes = new byte[payloadSize];
        for (int i = 0; i < payloadSize; i++) {
            bytes[i] = 'a';
        }
        String hugeValue = new String(bytes, StandardCharsets.UTF_8);

        SculptureRequest request = new SculptureRequest(
                "Huge Scene",
                hugeValue,
                "{\"units\":\"cm\"}",
                List.of("huge"),
                null
        );

        mockMvc.perform(post("/api/sculptures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isPayloadTooLarge());
    }

    @WithMockUser
    @Test
    void shouldFilterByTag() throws Exception {
        Sculpture tagged = new Sculpture();
        tagged.setName("Tagged");
        tagged.setMetadata("{}");
        tagged.setSceneJson("{\"foo\":1}");
        tagged.setTags(List.of("featured"));
        tagged.setSlug("tagged-slug");

        Sculpture other = new Sculpture();
        other.setName("Other");
        other.setMetadata("{}");
        other.setSceneJson("{\"foo\":2}");
        other.setTags(List.of("backlog"));
        other.setSlug("other-slug");

        sculptureRepository.saveAll(List.of(tagged, other));

        mockMvc.perform(get("/api/sculptures")
                        .param("tag", "featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tagged"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldFetchBySlugWithoutAuth() throws Exception {
        Sculpture sculpture = new Sculpture();
        sculpture.setName("Shared");
        sculpture.setMetadata("{}");
        sculpture.setSceneJson("{\"foo\":3}");
        sculpture.setTags(List.of("shared"));
        sculpture.setSlug("shared-slug");
        sculptureRepository.save(sculpture);

        mockMvc.perform(get("/api/sculptures/slug/{slug}", "shared-slug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Shared"))
                .andExpect(jsonPath("$.slug").value("shared-slug"));
    }

    @WithMockUser
    @Test
    void shouldUpdateAndDeleteSculpture() throws Exception {
        Sculpture sculpture = new Sculpture();
        sculpture.setName("Mutable");
        sculpture.setMetadata("{}");
        sculpture.setSceneJson("{\"foo\":4}");
        sculpture.setTags(List.of("draft"));
        sculpture.setSlug("mutable-slug");
        sculpture = sculptureRepository.save(sculpture);

        SculptureRequest update = new SculptureRequest(
                "Mutable Updated",
                "{\"foo\":5}",
                "{\"units\":\"m\"}",
                List.of("draft", "ready"),
                "mutable-custom"
        );

        mockMvc.perform(put("/api/sculptures/{id}", sculpture.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mutable Updated"))
                .andExpect(jsonPath("$.slug").value("mutable-custom"));

        mockMvc.perform(delete("/api/sculptures/{id}", sculpture.getId()))
                .andExpect(status().isNoContent());
    }
}
