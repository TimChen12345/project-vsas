package com.vsas.dto;

import java.time.Instant;

public class ScrollPreviewResponse extends ScrollSummaryResponse {

    /** TEXT, IMAGE_BASE64, HEX, BINARY */
    private String previewKind;
    /** Text snippet, small base64 image, or hex dump — null if BINARY only */
    private String previewContent;

    public String getPreviewKind() {
        return previewKind;
    }

    public void setPreviewKind(String previewKind) {
        this.previewKind = previewKind;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public void copySummary(ScrollSummaryResponse s) {
        setId(s.getId());
        setScrollId(s.getScrollId());
        setName(s.getName());
        setUploaderIdKey(s.getUploaderIdKey());
        setUploaderFullName(s.getUploaderFullName());
        setUploadedAt(s.getUploadedAt());
        setUpdatedAt(s.getUpdatedAt());
        setSizeBytes(s.getSizeBytes());
        setMimeType(s.getMimeType());
        setOriginalFilename(s.getOriginalFilename());
        setDownloadCount(s.getDownloadCount());
        setUploadEventCount(s.getUploadEventCount());
    }
}
