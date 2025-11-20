package com.muse_ai.rest.cma;// package com.muse_ai.rest.cma;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/cma")
@CrossOrigin(origins = "*")
public class CMAImageProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "MuseAI-Student/1.0"); // algunos CDNs son mañosos

        HttpEntity<Void> req = new HttpEntity<>(headers);
        ResponseEntity<byte[]> resp = restTemplate.exchange(url, HttpMethod.GET, req, byte[].class);

        MediaType ct = resp.getHeaders().getContentType();
        HttpHeaders out = new HttpHeaders();
        out.setContentType(ct != null ? ct : MediaType.IMAGE_JPEG);
        out.setCacheControl(CacheControl.maxAge(java.time.Duration.ofDays(1)));

        return new ResponseEntity<>(resp.getBody(), out, HttpStatus.OK);
    }
}
