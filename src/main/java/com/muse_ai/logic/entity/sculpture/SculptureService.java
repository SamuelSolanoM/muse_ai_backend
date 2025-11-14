package com.muse_ai.logic.entity.sculpture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class SculptureService {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9-]");
    private final SculptureRepository sculptureRepository;

    public SculptureService(SculptureRepository sculptureRepository) {
        this.sculptureRepository = sculptureRepository;
    }

    public Sculpture create(SculptureWriteCommand command) {
        Sculpture sculpture = new Sculpture();
        applyCommand(sculpture, command);
        sculpture.setSlug(resolveSlug(command.slug(), command.name(), null));
        sculpture.setUpdatedAt(Instant.now());
        return sculptureRepository.save(sculpture);
    }

    public Sculpture update(UUID id, SculptureWriteCommand command) {
        Sculpture sculpture = sculptureRepository.findById(id)
                .orElseThrow(() -> new SculptureNotFoundException("Sculpture " + id + " not found"));
        applyCommand(sculpture, command);
        sculpture.setSlug(resolveSlug(command.slug(), command.name(), id));
        sculpture.setUpdatedAt(Instant.now());
        return sculptureRepository.save(sculpture);
    }

    public List<Sculpture> list(String tag) {
        if (StringUtils.hasText(tag)) {
            return sculptureRepository.findAllByTag(tag);
        }
        return sculptureRepository.findAllByOrderByUpdatedAtDesc();
    }

    public Sculpture get(UUID id) {
        return sculptureRepository.findById(id)
                .orElseThrow(() -> new SculptureNotFoundException("Sculpture " + id + " not found"));
    }

    public void delete(UUID id) {
        Sculpture sculpture = get(id);
        sculptureRepository.delete(sculpture);
    }

    public Sculpture replaceScene(UUID id, String sceneJson) {
        Sculpture sculpture = get(id);
        sculpture.setSceneJson(sceneJson);
        sculpture.setUpdatedAt(Instant.now());
        return sculptureRepository.save(sculpture);
    }

    public Optional<Sculpture> findBySlug(String slug) {
        return sculptureRepository.findBySlug(slug);
    }

    private void applyCommand(Sculpture sculpture, SculptureWriteCommand command) {
        sculpture.setName(command.name());
        sculpture.setMetadata(defaultMetadata(command.metadata()));
        sculpture.setSceneJson(command.sceneJson());
        sculpture.setTags(sanitizeTags(command.tags()));
    }

    private List<String> sanitizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(tag -> tag.length() > 60 ? tag.substring(0, 60) : tag)
                .distinct()
                .collect(Collectors.toList());
    }

    private String defaultMetadata(String metadata) {
        return StringUtils.hasText(metadata) ? metadata : "{}";
    }

    private String resolveSlug(String requestedSlug, String fallbackName, UUID currentId) {
        String base = StringUtils.hasText(requestedSlug) ? requestedSlug : fallbackName;
        String normalized = slugify(base);
        if (!StringUtils.hasText(normalized)) {
            normalized = "sculpture-" + UUID.randomUUID().toString().substring(0, 8);
        }
        String candidate = normalized;
        int guard = 0;
        while (slugExists(candidate, currentId) && guard < 10) {
            candidate = normalized + "-" + UUID.randomUUID().toString().substring(0, 6);
            guard++;
        }
        return candidate;
    }

    private boolean slugExists(String slug, UUID currentId) {
        if (!StringUtils.hasText(slug)) {
            return false;
        }
        if (currentId == null) {
            return sculptureRepository.existsBySlug(slug);
        }
        return sculptureRepository.existsBySlugAndIdNot(slug, currentId);
    }

    private String slugify(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = normalized.toLowerCase(Locale.ROOT)
                .replace(" ", "-");
        slug = NON_ALPHANUMERIC.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("-{2,}", "-");
        slug = slug.replaceAll("^-|-$", "");
        return slug;
    }
}
