package com.vsas.service;

import com.vsas.dto.ScrollFilePayload;
import com.vsas.dto.ScrollPreviewResponse;
import com.vsas.dto.ScrollSummaryResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ScrollService {

    List<ScrollSummaryResponse> search(
            String uploaderIdKey, String scrollId, String name, Instant uploadedFrom, Instant uploadedTo);

    ScrollPreviewResponse preview(long scrollDbId);

    ScrollFilePayload download(long scrollDbId, String username);

    ScrollSummaryResponse create(String username, String scrollId, String name, MultipartFile file);

    ScrollSummaryResponse update(
            long scrollDbId, String username, String scrollId, String name, MultipartFile fileOrNull);

    void delete(long scrollDbId, String username);
}
