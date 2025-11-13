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

        Sculpture saved = sculptureRepository.save(sculpture);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTags()).containsExactlyInAnyOrder("organic", "wip");
        assertThat(sculptureRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldFilterByTagIgnoringCase() {
        Sculpture matching = new Sculpture();
        matching.setName("Match");
        matching.setMetadata("{}");
        matching.setSceneJson("{\"foo\":1}");
        matching.setTags(List.of("Hologram"));
        matching.setSlug("match-slug");

        Sculpture other = new Sculpture();
        other.setName("Other");
        other.setMetadata("{}");
        other.setSceneJson("{\"foo\":2}");
        other.setTags(List.of("Portrait"));
        other.setSlug("other-slug");

        sculptureRepository.saveAll(List.of(matching, other));

        List<Sculpture> result = sculptureRepository.findAllByTag("hologram");

        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(Sculpture::getName)
                .isEqualTo("Match");
    }
}
