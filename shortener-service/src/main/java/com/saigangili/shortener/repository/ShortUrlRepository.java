package com.saigangili.shortener.repository;

import com.saigangili.shortener.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    Optional<ShortUrl> findByShortCodeAndStatus(String shortCode, ShortUrl.Status status);
}
