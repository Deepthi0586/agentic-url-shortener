package com.saigangili.shortener.controller;

import com.saigangili.shortener.model.ShortUrl;
import com.saigangili.shortener.service.ShortUrlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

// NOTE: API key authentication and per-key rate limiting are expected to be
// enforced by a filter/interceptor (e.g., extracting X-API-Key header,
// validating against ApiKey table, and applying rate_limit_tier) - out of
// scope for this pass. Endpoints below assume the owner id is resolved from
// the API key and passed in as a header for demonstration purposes.
// NOTE: Stats aggregation endpoint (GET /urls/{code}/stats) is out of scope
// for this pass since it depends on ClickEvent aggregation/reporting.
@RestController
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping("/urls")
    public ResponseEntity<CreateShortUrlResponse> createShortUrl(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody CreateShortUrlRequest request) {
        ShortUrl shortUrl = shortUrlService.createShortUrl(request.originalUrl(), apiKey, request.expiresAt());
        String fullShortUrl = "https://short.ly/" + shortUrl.getShortCode();
        CreateShortUrlResponse response = new CreateShortUrlResponse(shortUrl.getShortCode(), fullShortUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/urls/{code}")
    public ResponseEntity<UrlMetadataResponse> getMetadata(@RequestHeader("X-API-Key") String apiKey,
                                                            @PathVariable String code) {
        ShortUrl shortUrl = shortUrlService.getActiveByCode(code);
        // Click count summary would normally come from aggregated ClickEvent data;
        // returning 0 as a placeholder since analytics storage is out of scope.
        UrlMetadataResponse response = new UrlMetadataResponse(
                shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getCreatedAt(),
                0L);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalUrl = shortUrlService.resolveOriginalUrl(code);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @DeleteMapping("/urls/{code}")
    public ResponseEntity<Void> deleteShortUrl(@RequestHeader("X-API-Key") String apiKey,
                                                @PathVariable String code) {
        shortUrlService.deactivate(code, apiKey);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ShortUrlService.ShortUrlNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ShortUrlService.ShortUrlNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ShortUrlService.NotOwnerException.class)
    public ResponseEntity<String> handleForbidden(ShortUrlService.NotOwnerException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    public record CreateShortUrlRequest(String originalUrl, LocalDateTime expiresAt) {
    }

    public record CreateShortUrlResponse(String shortCode, String shortUrl) {
    }

    public record UrlMetadataResponse(String shortCode, String originalUrl, LocalDateTime createdAt, Long clickCount) {
    }
}
