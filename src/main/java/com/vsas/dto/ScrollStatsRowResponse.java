package com.vsas.dto;

import java.time.Instant;

/** Per-scroll stats for admin dashboard. */
public class ScrollStatsRowResponse {

    private Long scrollDbId;
    private String scrollId;
    private String name;
    private String uploaderIdKey;
    private String uploaderUsername;
    private Instant uploadedAt;
    private long downloadCount;
    private long uploadEventCount;
    private long sizeBytes;

    public ScrollStatsRowResponse() {}

    public Long getScrollDbId() {
        return scrollDbId;
    }

    public void setScrollDbId(Long scrollDbId) {
        this.scrollDbId = scrollDbId;
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

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
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

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}
