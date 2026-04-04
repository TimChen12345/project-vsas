package com.vsas.dto;

public class ScrollFilePayload {

    private final byte[] data;
    private final String filename;
    private final String mimeType;

    public ScrollFilePayload(byte[] data, String filename, String mimeType) {
        this.data = data;
        this.filename = filename;
        this.mimeType = mimeType;
    }

    public byte[] getData() {
        return data;
    }

    public String getFilename() {
        return filename;
    }

    public String getMimeType() {
        return mimeType;
    }
}
