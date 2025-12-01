package com.muse_ai.rest.cma;

import com.muse_ai.logic.external.cma.CMAService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;   

@RestController
@RequestMapping("/api/cma")
@CrossOrigin(origins = "*")
public class CMARestController {

    private final CMAService cmaService;

    public CMARestController(CMAService cmaService) {
        this.cmaService = cmaService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchArtworks(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String period
    ) {
        return ResponseEntity.ok(
                cmaService.search(type, q, period)
        );
    }

    @GetMapping("/artwork/{id}")
    public ResponseEntity<?> getArtwork(@PathVariable String id) {
        return ResponseEntity.ok(cmaService.getArtwork(id));
    }
}
