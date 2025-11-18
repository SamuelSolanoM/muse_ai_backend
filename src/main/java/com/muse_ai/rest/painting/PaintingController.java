package com.muse_ai.rest.painting;

import com.muse_ai.logic.entity.painting.Painting;
import com.muse_ai.logic.entity.painting.PaintingNotFoundException;
import com.muse_ai.logic.entity.painting.PaintingService;
import com.muse_ai.logic.entity.painting.PaintingWriteCommand;
import com.muse_ai.rest.painting.dto.PaintingRequest;
import com.muse_ai.rest.painting.dto.PaintingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paintings")
@CrossOrigin(origins = {"${app.frontend.origin:http://localhost:4200}"})
@Validated
public class PaintingController {

    private static final int MAX_SCENE_BYTES = 5 * 1024 * 1024; // 5 MB
    private final PaintingService paintingService;

    public PaintingController(PaintingService paintingService) {
        this.paintingService = paintingService;
    }

    @PostMapping
    public ResponseEntity<PaintingResponse> create(@Valid @RequestBody PaintingRequest request) {
        validateScenePayloadSize(request.sceneJson());
        Painting painting = paintingService.create(toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(PaintingResponse.from(painting));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaintingResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody PaintingRequest request) {
        validateScenePayloadSize(request.sceneJson());
        Painting painting = paintingService.update(id, toCommand(request));
        return ResponseEntity.ok(PaintingResponse.from(painting));
    }

    @GetMapping
    public ResponseEntity<List<PaintingResponse>> list(@RequestParam(required = false) String tag) {
        List<PaintingResponse> response = paintingService.list(tag)
                .stream()
                .map(PaintingResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaintingResponse> getById(@PathVariable UUID id) {
        Painting painting = paintingService.get(id);
        return ResponseEntity.ok(PaintingResponse.from(painting));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        paintingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PaintingResponse> getBySlug(@PathVariable String slug) {
        Painting painting = paintingService.findBySlug(slug)
                .orElseThrow(() -> new PaintingNotFoundException("Painting " + slug + " not found"));
        return ResponseEntity.ok(PaintingResponse.from(painting));
    }

    // ───── Helpers ─────

    private PaintingWriteCommand toCommand(PaintingRequest request) {
        return new PaintingWriteCommand(
                request.name(),
                request.metadata(),
                request.sceneJson(),
                request.tags(),
                request.slug()
        );
    }

    private void validateScenePayloadSize(String sceneJson) {
        if (sceneJson == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sceneJson is required");
        }
        int bytes = sceneJson.getBytes(StandardCharsets.UTF_8).length;
        if (bytes > MAX_SCENE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "sceneJson exceeds the 5 MB limit (" + bytes + " bytes provided)"
            );
        }
    }
}
