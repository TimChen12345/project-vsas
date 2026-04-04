package com.vsas.dto;

import java.time.Instant;

public class ScrollSummaryResponse {

    private Long id;
    private String scrollId;
    private String name;
    private String uploaderIdKey;
    private String uploaderUsername;
    private String uploaderFullName;
    private Instant uploadedAt;
    private Instant updatedAt;
    private long sizeBytes;
    private String mimeType;
    private String originalFilename;
    private long downloadCount;
    private long uploadEventCount;

    public ScrollSummaryResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUploaderIdKey() {
        return uploaderIdKey;
    }

    public void setUploaderIdKey(String uploaderIdKey) {
        this.uploaderIdKey = uploaderIdKey;
    }

    public String getUploaderUsername() {
        return uploaderUsername;
    }

    public void setUploaderUsername(String uploaderUsername) {
        this.uploaderUsername = uploaderUsername;
    }

    public String getUploaderFullName() {
        return uploaderFullName;
    }

    public void setUploaderFullName(String uploaderFullName) {
        this.uploaderFullName = uploaderFullName;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public long getUploadEventCount() {
        return uploadEventCount;
    }

    public void setUploadEventCount(long uploadEventCount) {
        this.uploadEventCount = uploadEventCount;
    }
}
