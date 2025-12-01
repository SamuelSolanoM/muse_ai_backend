package com.muse_ai.logic.entity.painting;

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
public class PaintingService {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9-]");
    private final PaintingRepository paintingRepository;

    public PaintingService(PaintingRepository paintingRepository) {
        this.paintingRepository = paintingRepository;
    }

    public Painting create(PaintingWriteCommand command) {
        Painting painting = new Painting();
        applyCommand(painting, command);
        painting.setSlug(resolveSlug(command.slug(), command.name(), null));
        painting.setUpdatedAt(Instant.now());
        return paintingRepository.save(painting);
    }

    public Painting update(UUID id, PaintingWriteCommand command) {
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new PaintingNotFoundException("Painting " + id + " not found"));
        applyCommand(painting, command);
        painting.setSlug(resolveSlug(command.slug(), command.name(), id));
        painting.setUpdatedAt(Instant.now());
        return paintingRepository.save(painting);
    }

    public List<Painting> list(String tag) {
        if (StringUtils.hasText(tag)) {
            return paintingRepository.findAllByTag(tag);
        }
        return paintingRepository.findAllByOrderByUpdatedAtDesc();
    }

    public Painting get(UUID id) {
        return paintingRepository.findById(id)
                .orElseThrow(() -> new PaintingNotFoundException("Painting " + id + " not found"));
    }

    public void delete(UUID id) {
        Painting painting = get(id);
        paintingRepository.delete(painting);
    }

    public Painting replaceScene(UUID id, String sceneJson) {
        Painting painting = get(id);
        painting.setSceneJson(sceneJson);
        painting.setUpdatedAt(Instant.now());
        return paintingRepository.save(painting);
    }

    public Optional<Painting> findBySlug(String slug) {
        return paintingRepository.findBySlug(slug);
    }

    private void applyCommand(Painting painting, PaintingWriteCommand command) {
        painting.setName(command.name());
        painting.setMetadata(defaultMetadata(command.metadata()));
        painting.setSceneJson(command.sceneJson());
        painting.setTags(sanitizeTags(command.tags()));
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
            normalized = "painting-" + UUID.randomUUID().toString().substring(0, 8);
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
            return paintingRepository.existsBySlug(slug);
        }
        return paintingRepository.existsBySlugAndIdNot(slug, currentId);
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
