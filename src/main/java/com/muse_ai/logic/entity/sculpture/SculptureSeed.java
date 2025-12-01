package com.muse_ai.logic.entity.sculpture;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class SculptureSeed implements CommandLineRunner {

    private final SculptureService service;

    @Override
    public void run(String... args) {
        if (service.list(null).isEmpty()) {
            var meta = """
        {
          "glbUrl": "/assets/models/plant.glb",
          "scale": 1.0,
          "rotY": 0.0,
          "offsetY": 0.05
        }
      """;
            var cmd = new SculptureWriteCommand(
                    "Obra maestra",
                    meta,
                    "{}",                 // sceneJson si no lo usas, deja "{}"
                    List.of("demo"),
                    "obra-maestra",
                    "Semilla de ejemplo"
            );
            service.create(cmd);
        }
    }
}

