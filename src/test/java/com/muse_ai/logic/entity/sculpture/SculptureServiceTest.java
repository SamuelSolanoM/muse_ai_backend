package com.muse_ai.logic.entity.sculpture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SculptureServiceTest {

    @Mock
    private SculptureRepository sculptureRepository;

    private SculptureService sculptureService;

    @BeforeEach
    void setUp() {
        sculptureService = new SculptureService(sculptureRepository);
    }

    @Test
    void createShouldPersistDescription() {
        when(sculptureRepository.save(any(Sculpture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SculptureWriteCommand command = new SculptureWriteCommand(
                "Service Sculpture",
                "{\"meta\":true}",
                "{\"scene\":{}}",
                List.of("one"),
                null,
                "Rich description"
        );

        Sculpture saved = sculptureService.create(command);

        assertThat(saved.getDescription()).isEqualTo("Rich description");
    }

    @Test
    void updateShouldRetainExistingDescriptionWhenMissing() {
        UUID id = UUID.randomUUID();
        Sculpture existing = new Sculpture();
        existing.setId(id);
        existing.setName("Existing");
        existing.setMetadata("{}");
        existing.setSceneJson("{\"foo\":1}");
        existing.setDescription("Already detailed");

        when(sculptureRepository.findById(id)).thenReturn(Optional.of(existing));
        when(sculptureRepository.save(any(Sculpture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SculptureWriteCommand command = new SculptureWriteCommand(
                "Existing",
                "{}",
                "{\"foo\":2}",
                List.of("tag"),
                "existing-slug",
                null
        );

        Sculpture updated = sculptureService.update(id, command);

        assertThat(updated.getDescription()).isEqualTo("Already detailed");
    }
}
