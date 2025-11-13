package com.muse_ai.rest.sculpture;

import com.muse_ai.logic.entity.sculpture.Sculpture;
import com.muse_ai.logic.entity.sculpture.SculptureNotFoundException;
import com.muse_ai.logic.entity.sculpture.SculptureService;
import com.muse_ai.logic.entity.sculpture.SculptureWriteCommand;
import com.muse_ai.rest.sculpture.dto.SculptureRequest;
import com.muse_ai.rest.sculpture.dto.SculptureResponse;
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
@RequestMapping("/api/sculptures")
@CrossOrigin(origins = {"${app.frontend.origin:http://localhost:4200}"})
@Validated
public class SculptureController {

    private static final int MAX_SCENE_BYTES = 5 * 1024 * 1024;
    private final SculptureService sculptureService;

    public SculptureController(SculptureService sculptureService) {
        this.sculptureService = sculptureService;
    }

    @PostMapping
    public ResponseEntity<SculptureResponse> create(@Valid @RequestBody SculptureRequest request) {
        validateScenePayloadSize(request.sceneJson());
        Sculpture sculpture = sculptureService.create(toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(SculptureResponse.from(sculpture));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SculptureResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody SculptureRequest request) {
        validateScenePayloadSize(request.sceneJson());
        Sculpture sculpture = sculptureService.update(id, toCommand(request));
        return ResponseEntity.ok(SculptureResponse.from(sculpture));
    }

    @GetMapping
    public ResponseEntity<List<SculptureResponse>> list(@RequestParam(required = false) String tag) {
        List<SculptureResponse> response = sculptureService.list(tag)
                .stream()
                .map(SculptureResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SculptureResponse> getById(@PathVariable UUID id) {
        Sculpture sculpture = sculptureService.get(id);
        return ResponseEntity.ok(SculptureResponse.from(sculpture));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sculptureService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<SculptureResponse> getBySlug(@PathVariable String slug) {
        Sculpture sculpture = sculptureService.findBySlug(slug)
                .orElseThrow(() -> new SculptureNotFoundException("Sculpture " + slug + " not found"));
        return ResponseEntity.ok(SculptureResponse.from(sculpture));
    }

    private SculptureWriteCommand toCommand(SculptureRequest request) {
        return new SculptureWriteCommand(
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
