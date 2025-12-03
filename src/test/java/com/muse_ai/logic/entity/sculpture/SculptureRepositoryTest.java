package com.muse_ai.logic.entity.sculpture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SculptureRepositoryTest {

    @Autowired
    private SculptureRepository sculptureRepository;

    @Test
    void shouldPersistAndRetrieveSculpture() {
        Sculpture sculpture = new Sculpture();
        sculpture.setName("Repository Sample");
        sculpture.setMetadata("{\"units\":\"cm\"}");
        sculpture.setSceneJson("{\"foo\":\"bar\"}");
        sculpture.setTags(List.of("organic", "wip"));
        sculpture.setSlug("repository-sample");
        sculpture.setDescription("Repository level description");

        Sculpture saved = sculptureRepository.save(sculpture);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTags()).containsExactlyInAnyOrder("organic", "wip");
        assertThat(sculptureRepository.findById(saved.getId())).isPresent();
    }
}
