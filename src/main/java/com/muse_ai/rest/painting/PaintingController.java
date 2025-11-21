package com.muse_ai.rest.painting;

import com.muse_ai.logic.entity.painting.*;
import com.muse_ai.rest.painting.dto.PaintingRequest;
import com.muse_ai.rest.painting.dto.PaintingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paintings")
@CrossOrigin(origins = {"${app.frontend.origin:http://localhost:4200}"})
@Validated
public class PaintingController {

    private final PaintingService paintingService;

    public PaintingController(PaintingService paintingService) {
        this.paintingService = paintingService;
    }

    @PostMapping
    public ResponseEntity<PaintingResponse> create(@Valid @RequestBody PaintingRequest request) {
        Painting painting = paintingService.create(toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(PaintingResponse.from(painting));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaintingResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody PaintingRequest request) {
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

    private PaintingWriteCommand toCommand(PaintingRequest request) {
        return new PaintingWriteCommand(
                request.name(),
                request.metadata(),
                request.sceneJson(),
                request.tags(),
                request.slug()
        );
    }
}
