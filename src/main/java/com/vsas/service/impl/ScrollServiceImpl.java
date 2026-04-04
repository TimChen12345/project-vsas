package com.vsas.service.impl;

import com.vsas.dto.ScrollFilePayload;
import com.vsas.dto.ScrollPreviewResponse;
import com.vsas.dto.ScrollSummaryResponse;
import com.vsas.entity.Scroll;
import com.vsas.entity.User;
import com.vsas.repository.ScrollRepository;
import com.vsas.repository.ScrollSpecifications;
import com.vsas.repository.UserRepository;
import com.vsas.service.ScrollService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ScrollServiceImpl implements ScrollService {

    private static final int TEXT_PREVIEW_CHARS = 6000;
    private static final int HEX_PREVIEW_BYTES = 384;
    private static final int IMAGE_PREVIEW_MAX_BYTES = 350_000;

    private final ScrollRepository scrollRepository;
    private final UserRepository userRepository;

    public ScrollServiceImpl(ScrollRepository scrollRepository, UserRepository userRepository) {
        this.scrollRepository = scrollRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrollSummaryResponse> search(
            String uploaderIdKey, String scrollId, String name, Instant uploadedFrom, Instant uploadedTo) {
        return scrollRepository
                .findAll(ScrollSpecifications.filtered(uploaderIdKey, scrollId, name, uploadedFrom, uploadedTo))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ScrollPreviewResponse preview(long scrollDbId) {
        Scroll s = scrollRepository
                .findById(scrollDbId)
                .orElseThrow(() -> new IllegalArgumentException("Scroll not found"));
        ScrollSummaryResponse sum = toSummary(s);
        ScrollPreviewResponse r = new ScrollPreviewResponse();
        r.copySummary(sum);
        byte[] data = s.getData();
        if (data == null || data.length == 0) {
            r.setPreviewKind("BINARY");
            r.setPreviewContent(null);
            return r;
        }
        String mime = s.getMimeType() != null ? s.getMimeType().toLowerCase(Locale.ROOT) : "";
        if (mime.startsWith("text/") || mime.contains("json") || mime.contains("xml")) {
            r.setPreviewKind("TEXT");
            String text = new String(data, StandardCharsets.UTF_8);
            if (text.length() > TEXT_PREVIEW_CHARS) {
                text = text.substring(0, TEXT_PREVIEW_CHARS) + "\n… (truncated)";
            }
            r.setPreviewContent(text);
            return r;
        }
        if (mime.startsWith("image/")) {
            int len = Math.min(data.length, IMAGE_PREVIEW_MAX_BYTES);
            r.setPreviewKind("IMAGE_BASE64");
            r.setPreviewContent(Base64.getEncoder().encodeToString(java.util.Arrays.copyOf(data, len)));
            return r;
        }
        r.setPreviewKind("HEX");
        int n = Math.min(data.length, HEX_PREVIEW_BYTES);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(String.format("%02x ", data[i]));
            if ((i + 1) % 24 == 0) {
                sb.append("\n");
            }
        }
        if (data.length > n) {
            sb.append("\n… (truncated)");
        }
        r.setPreviewContent(sb.toString().trim());
        return r;
    }

    @Override
    @Transactional
    public ScrollFilePayload download(long scrollDbId, String username) {
        Scroll s = scrollRepository
                .findById(scrollDbId)
                .orElseThrow(() -> new IllegalArgumentException("Scroll not found"));
        s.setDownloadCount(s.getDownloadCount() + 1);
        scrollRepository.save(s);
        String mime = s.getMimeType() != null ? s.getMimeType() : "application/octet-stream";
        return new ScrollFilePayload(s.getData(), s.getOriginalFilename(), mime);
    }

    @Override
    @Transactional
    public ScrollSummaryResponse create(String username, String scrollId, String name, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        String sid = scrollId != null ? scrollId.trim() : "";
        String nm = name != null ? name.trim() : "";
        if (sid.isEmpty() || nm.isEmpty()) {
            throw new IllegalArgumentException("Scroll ID and name are required");
        }
        if (scrollRepository.existsByScrollId(sid)) {
            throw new IllegalArgumentException("Scroll ID already in use");
        }
        if (scrollRepository.existsByName(nm)) {
            throw new IllegalArgumentException("Scroll name already in use");
        }
        User uploader = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Scroll s = new Scroll();
        s.setScrollId(sid);
        s.setName(nm);
        s.setUploader(uploader);
        try {
            s.setData(file.getBytes());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read file");
        }
        s.setOriginalFilename(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin");
        s.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        scrollRepository.save(s);
        return toSummary(s);
    }

    @Override
    @Transactional
    public ScrollSummaryResponse update(
            long scrollDbId, String username, String scrollId, String name, MultipartFile fileOrNull) {
        Scroll s = scrollRepository
                .findById(scrollDbId)
                .orElseThrow(() -> new IllegalArgumentException("Scroll not found"));
        if (!s.getUploader().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only edit scrolls you uploaded");
        }
        if (scrollId != null && !scrollId.isBlank()) {
            String sid = scrollId.trim();
            if (!sid.equals(s.getScrollId()) && scrollRepository.existsByScrollIdAndIdNot(sid, s.getId())) {
                throw new IllegalArgumentException("Scroll ID already in use");
            }
            s.setScrollId(sid);
        }
        if (name != null && !name.isBlank()) {
            String nm = name.trim();
            if (!nm.equals(s.getName()) && scrollRepository.existsByNameAndIdNot(nm, s.getId())) {
                throw new IllegalArgumentException("Scroll name already in use");
            }
            s.setName(nm);
        }
        if (fileOrNull != null && !fileOrNull.isEmpty()) {
            try {
                s.setData(fileOrNull.getBytes());
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not read file");
            }
            if (fileOrNull.getOriginalFilename() != null) {
                s.setOriginalFilename(fileOrNull.getOriginalFilename());
            }
            if (fileOrNull.getContentType() != null) {
                s.setMimeType(fileOrNull.getContentType());
            }
            s.setUploadEventCount(s.getUploadEventCount() + 1);
        }
        return toSummary(s);
    }

    @Override
    @Transactional
    public void delete(long scrollDbId, String username) {
        Scroll s = scrollRepository
                .findById(scrollDbId)
                .orElseThrow(() -> new IllegalArgumentException("Scroll not found"));
        if (!s.getUploader().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only remove scrolls you uploaded");
        }
        scrollRepository.delete(s);
    }

    private ScrollSummaryResponse toSummary(Scroll s) {
        User u = s.getUploader();
        ScrollSummaryResponse r = new ScrollSummaryResponse();
        r.setId(s.getId());
        r.setScrollId(s.getScrollId());
        r.setName(s.getName());
        r.setUploaderIdKey(u.getIdKey());
        r.setUploaderUsername(u.getUsername());
        r.setUploaderFullName(u.getFullName() != null ? u.getFullName() : u.getUsername());
        r.setUploadedAt(s.getUploadedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        r.setSizeBytes(s.getData() != null ? s.getData().length : 0);
        r.setMimeType(s.getMimeType());
        r.setOriginalFilename(s.getOriginalFilename());
        r.setDownloadCount(s.getDownloadCount());
        r.setUploadEventCount(s.getUploadEventCount());
        return r;
    }
}
