package com.saigangili.shortener.controller;

import com.saigangili.shortener.model.ShortUrl;
import com.saigangili.shortener.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/urls")
public class ShortUrlController {

    // Note: existing auth middleware/token check is assumed to be applied
    // via security filter chain configured elsewhere in the project.

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    @PostMapping
    public ResponseEntity<?> createShortUrl(@RequestBody CreateShortUrlRequest request,
                                             Authentication authentication) {
        if (request.targetUrl() == null || request.targetUrl().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID_TARGET_URL", "targetUrl is required"));
        }

        String createdBy = authentication != null ? authentication.getName() : null;

        try {
            ShortUrl created = shortUrlService.createShortUrl(
                    request.targetUrl(), request.customAlias(), createdBy);

            ShortUrlResponse body = new ShortUrlResponse(
                    created.getShortCode(),
                    created.getTargetUrl(),
                    created.isCustom());

            return ResponseEntity.created(URI.create("/" + created.getShortCode())).body(body);

        } catch (ShortUrlService.InvalidAliasException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("INVALID_ALIAS", e.getMessage()));
        } catch (ShortUrlService.AliasAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("ALIAS_ALREADY_EXISTS", e.getMessage()));
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        // Note: redirect-path caching remains unchanged / out of scope for this pass.
        ShortUrl shortUrl = shortUrlService.getByShortCode(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(shortUrl.getTargetUrl()))
                .build();
    }

    public record CreateShortUrlRequest(String targetUrl, String customAlias) {
    }

    public record ShortUrlResponse(String shortCode, String targetUrl, boolean isCustom) {
    }

    public record ErrorResponse(String errorCode, String message) {
    }
}
