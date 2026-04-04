package com.vsas.controller;

import com.vsas.dto.ScrollFilePayload;
import com.vsas.dto.ScrollPreviewResponse;
import com.vsas.dto.ScrollSummaryResponse;
import com.vsas.service.ScrollService;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/scrolls")
public class ScrollController {

    private final ScrollService scrollService;

    public ScrollController(ScrollService scrollService) {
        this.scrollService = scrollService;
    }

    @GetMapping
    public List<ScrollSummaryResponse> search(
            @RequestParam(required = false) String uploaderIdKey,
            @RequestParam(required = false) String scrollId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant uploadedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant uploadedTo) {
        return scrollService.search(uploaderIdKey, scrollId, name, uploadedFrom, uploadedTo);
    }

    @GetMapping("/{id}/preview")
    public ScrollPreviewResponse preview(@PathVariable("id") Long id) {
        return scrollService.preview(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable("id") Long id, Authentication authentication) {
        ScrollFilePayload f = scrollService.download(id, authentication.getName());
        ContentDisposition disposition =
                ContentDisposition.attachment().filename(f.getFilename()).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(f.getMimeType()))
                .body(f.getData());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ScrollSummaryResponse> create(
            Authentication authentication,
            @RequestParam String scrollId,
            @RequestParam String name,
            @RequestParam("file") MultipartFile file) {
        ScrollSummaryResponse created =
                scrollService.create(authentication.getName(), scrollId, name, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ScrollSummaryResponse update(
            @PathVariable("id") Long id,
            Authentication authentication,
            @RequestParam(required = false) String scrollId,
            @RequestParam(required = false) String name,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return scrollService.update(id, authentication.getName(), scrollId, name, file);
    }

    @DeleteMapping("/{id}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id, Authentication authentication) {
        scrollService.delete(id, authentication.getName());
    }
}
