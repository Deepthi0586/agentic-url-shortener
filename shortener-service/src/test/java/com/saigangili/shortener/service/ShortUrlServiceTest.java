package com.saigangili.shortener.service;

import com.saigangili.shortener.model.UrlMapping;
import com.saigangili.shortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    private ShortUrlService shortUrlService;

    @BeforeEach
    void setUp() {
        shortUrlService = new ShortUrlService(urlMappingRepository);
    }

    @Test
    void createShortUrl_withBlankOriginalUrl_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> shortUrlService.createShortUrl("   ", null, null, "client-1"));
        verifyNoInteractions(urlMappingRepository);
    }

    @Test
    void createShortUrl_withoutCustomAlias_generatesUniqueShortCodeAndSaves() {
        when(urlMappingRepository.existsByShortCode(anyString())).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UrlMapping result = shortUrlService.createShortUrl("https://example.com", null, null, "client-1");

        assertNotNull(result);
        assertNotNull(result.getShortCode());
        assertFalse(result.getShortCode().isBlank());
        assertFalse(result.isCustomAlias());
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals(UrlMapping.Status.ACTIVE, result.getStatus());
        assertEquals("client-1", result.getCreatedBy());

        verify(urlMappingRepository, atLeastOnce()).existsByShortCode(anyString());
        verify(urlMappingRepository).save(any(UrlMapping.class));
    }

    @Test
    void createShortUrl_withValidCustomAlias_usesAliasAsShortCode() {
        when(urlMappingRepository.existsByShortCode("my-alias")).thenReturn(false);
        when(urlMappingRepository.save(any(UrlMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UrlMapping result = shortUrlService.createShortUrl("https://example.com", "my-alias", null, "client-1");

        assertEquals("my-alias", result.getShortCode());
        assertTrue(result.isCustomAlias());
        verify(urlMappingRepository).existsByShortCode("my-alias");
        verify(urlMappingRepository).save(any(UrlMapping.class));
    }

    @Test
    void createShortUrl_withInvalidCustomAlias_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> shortUrlService.createShortUrl("https://example.com", "ab", null, "client-1"));
        verify(urlMappingRepository, never()).existsByShortCode(anyString());
        verify(urlMappingRepository, never()).save(any(UrlMapping.class));
    }

    @Test
    void createShortUrl_withAliasAlreadyInUse_throwsIllegalStateException() {
        when(urlMappingRepository.existsByShortCode("taken")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> shortUrlService.createShortUrl("https://example.com", "taken", null, "client-1"));

        assertTrue(ex.getMessage().contains("taken"));
        verify(urlMappingRepository, never()).save(any(UrlMapping.class));
    }

    @Test
    void getMetadata_whenFound_returnsMapping() {
        UrlMapping mapping = new UrlMapping("abc123", "https://example.com", false,
                LocalDateTime.now(), null, UrlMapping.Status.ACTIVE, "client-1");
        when(urlMappingRepository.findByShortCode("abc123")).thenReturn(Optional.of(mapping));

        Optional<UrlMapping> result = shortUrlService.getMetadata("abc123");

        assertTrue(result.isPresent());
        assertEquals("abc123", result.get().getShortCode());
    }

    @Test
    void getMetadata_whenNotFound_returnsEmpty() {
        when(urlMappingRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        Optional<UrlMapping> result = shortUrlService.getMetadata("missing");

        assertTrue(result.isEmpty());
    }

    @Test
    void resolveForRedirect_whenNotFound_returnsEmpty() {
        when(urlMappingRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        Optional<UrlMapping> result = shortUrlService.resolveForRedirect("missing");

        assertTrue(result.isEmpty());
        verify(urlMappingRepository, never()).save(any(UrlMapping.class));
    }

    @Test
    void resolveForRedirect_whenDeleted_returnsEmpty() {
        UrlMapping mapping = new UrlMapping("abc123", "https://example.com", false,
                LocalDateTime.now(), null, UrlMapping.Status.DELETED, "client-1");
        when(urlMappingRepository.findByShortCode("abc123")).thenReturn(Optional.of(mapping));

        Optional<UrlMapping> result = shortUrlService.resolveForRedirect("abc123");

        assertTrue(result.isEmpty());
        verify(urlMappingRepository, never()).save(any(UrlMapping.class));
    }

    @Test
    void resolveForRedirect_whenExpired_marksExpiredAndReturnsEmpty() {
        UrlMapping mapping = new UrlMapping("abc123", "https://example.com", false,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusHours(1),
                UrlMapping.Status.ACTIVE, "client-1");
        when(urlMappingRepository.findByShortCode("abc123")).thenReturn(Optional.of(mapping));

        Optional<UrlMapping> result = shortUrlService.resolveForRedirect("abc123");

        assertTrue(result.isEmpty());

        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepository).save(captor.capture());
        assertEquals(UrlMapping.Status.EXPIRED, captor.getValue().getStatus());
    }

    @Test
    void resolveForRedirect_whenActiveAndNotExpired_returnsMapping() {
        UrlMapping mapping = new UrlMapping("abc123", "https://example.com", false,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                UrlMapping.Status.ACTIVE, "client-1");
        when(urlMappingRepository.findByShortCode("abc123")).thenReturn(Optional.of(mapping));

        Optional<UrlMapping> result = shortUrlService.resolveForRedirect("abc123");

        assertTrue(result.isPresent());
        assertEquals("https://example.com", result.get().getOriginalUrl());
        verify(urlMappingRepository, never()).save(any(UrlMapping.class));
    }

    @Test
    void deactivate_whenFound_setsDeletedStatusAndReturnsTrue() {
        UrlMapping mapping = new UrlMapping("abc123", "https://example.com", false,
                LocalDateTime.now(), null, UrlMapping.Status.ACTIVE, "client-1");
        when(urlMappingRepository.findByShortCode("abc123")).thenReturn(Optional.of(mapping));

        boolean result = shortUrlService.deactivate("abc123");

        assertTrue(result);
        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        verify(urlMappingRepository).save(captor.capture());
        assertEquals(UrlMapping.Status.DELETED, captor.getValue().getStatus());
    }

    @Test
    void deactivate_whenNotFound_returnsFalse() {
        when(urlMappingRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        boolean result = shortUrlService.deactivate("missing");

        assertFalse(result);
        verify(urlMappingRepository, never()).save(any(UrlMapping.class));
    }
}
