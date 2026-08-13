package com.saigangili.shortener.service;

import com.saigangili.shortener.model.ShortUrl;
import com.saigangili.shortener.repository.ShortUrlRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class ShortUrlService {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final ShortUrlRepository shortUrlRepository;
    private final SecureRandom random = new SecureRandom();

    public ShortUrlService(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    // NOTE: Redis read-through caching (cache short_code -> original_url on
    // create and on first redirect lookup) is out of scope for this pass.
    // NOTE: Async click event logging / queue publishing on redirect is also
    // out of scope for this pass.

    public ShortUrl createShortUrl(String originalUrl, String apiKeyOwnerId, LocalDateTime expiresAt) {
        String code = generateUniqueShortCode();
        ShortUrl shortUrl = new ShortUrl(code, originalUrl, apiKeyOwnerId, expiresAt);
        return shortUrlRepository.save(shortUrl);
    }

    public ShortUrl getActiveByCode(String code) {
        return shortUrlRepository.findByShortCodeAndStatus(code, ShortUrl.Status.ACTIVE)
                .orElseThrow(() -> new ShortUrlNotFoundException(code));
    }

    public String resolveOriginalUrl(String code) {
        ShortUrl shortUrl = getActiveByCode(code);
        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ShortUrlNotFoundException(code);
        }
        // Analytics logging for this redirect would be dispatched asynchronously
        // here (e.g., to a queue) - out of scope for this pass.
        return shortUrl.getOriginalUrl();
    }

    public void deactivate(String code, String ownerId) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(code)
                .orElseThrow(() -> new ShortUrlNotFoundException(code));
        if (!shortUrl.getCreatedBy().equals(ownerId)) {
            throw new NotOwnerException(code);
        }
        shortUrl.setStatus(ShortUrl.Status.DELETED);
        shortUrlRepository.save(shortUrl);
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = randomCode();
            if (!shortUrlRepository.existsByShortCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate a unique short code after " + MAX_GENERATION_ATTEMPTS + " attempts");
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static class ShortUrlNotFoundException extends RuntimeException {
        public ShortUrlNotFoundException(String code) {
            super("Short URL not found or inactive: " + code);
        }
    }

    public static class NotOwnerException extends RuntimeException {
        public NotOwnerException(String code) {
            super("Caller is not the owner of short URL: " + code);
        }
    }
}
