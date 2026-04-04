package com.vsas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vsas.dto.ScrollFilePayload;
import com.vsas.dto.ScrollPreviewResponse;
import com.vsas.dto.ScrollSummaryResponse;
import com.vsas.entity.Scroll;
import com.vsas.entity.User;
import com.vsas.repository.ScrollRepository;
import com.vsas.repository.UserRepository;
import com.vsas.service.impl.ScrollServiceImpl;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ScrollServiceImplTest {

    @Mock private ScrollRepository scrollRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ScrollServiceImpl scrollService;

    private User uploader;
    private Scroll scroll;

    @BeforeEach
    void setUp() {
        uploader = new User();
        uploader.setId(1L);
        uploader.setUsername("bob");
        uploader.setIdKey("bob-k");
        uploader.setFullName("Bob");

        scroll = new Scroll();
        scroll.setId(10L);
        scroll.setScrollId("sid-1");
        scroll.setName("Scroll One");
        scroll.setUploader(uploader);
        scroll.setUploadedAt(Instant.now());
        scroll.setUpdatedAt(Instant.now());
        scroll.setDownloadCount(0);
        scroll.setUploadEventCount(1);
        scroll.setData("hello text".getBytes(StandardCharsets.UTF_8));
        scroll.setOriginalFilename("f.txt");
        scroll.setMimeType("text/plain");
    }

    @Test
    void search_delegatesToRepository() {
        when(scrollRepository.findAll(any(Specification.class))).thenReturn(List.of(scroll));

        List<ScrollSummaryResponse> rows = scrollService.search(null, null, null, null, null);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getScrollId()).isEqualTo("sid-1");
    }

    @Test
    void preview_text_truncatesLongContent() {
        byte[] big = "x".repeat(7000).getBytes(StandardCharsets.UTF_8);
        scroll.setData(big);
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        ScrollPreviewResponse p = scrollService.preview(10L);

        assertThat(p.getPreviewKind()).isEqualTo("TEXT");
        assertThat(p.getPreviewContent()).contains("truncated");
    }

    @Test
    void preview_emptyData_binaryKind() {
        scroll.setData(new byte[0]);
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        ScrollPreviewResponse p = scrollService.preview(10L);

        assertThat(p.getPreviewKind()).isEqualTo("BINARY");
        assertThat(p.getPreviewContent()).isNull();
    }

    @Test
    void preview_image_base64() {
        scroll.setData(new byte[] {1, 2, 3});
        scroll.setMimeType("image/png");
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        ScrollPreviewResponse p = scrollService.preview(10L);

        assertThat(p.getPreviewKind()).isEqualTo("IMAGE_BASE64");
        assertThat(p.getPreviewContent()).isNotBlank();
    }

    @Test
    void preview_hexForBinary() {
        scroll.setData(new byte[] {0x0f, (byte) 0xff});
        scroll.setMimeType("application/octet-stream");
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        ScrollPreviewResponse p = scrollService.preview(10L);

        assertThat(p.getPreviewKind()).isEqualTo("HEX");
        assertThat(p.getPreviewContent()).contains("0f");
    }

    @Test
    void download_incrementsCount() {
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        ScrollFilePayload f = scrollService.download(10L, "bob");

        assertThat(f.getFilename()).isEqualTo("f.txt");
        assertThat(scroll.getDownloadCount()).isEqualTo(1);
        verify(scrollRepository).save(scroll);
    }

    @Test
    void create_requiresFile() {
        assertThatThrownBy(() -> scrollService.create("bob", "s", "n", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File");
    }

    @Test
    void create_requiresScrollIdAndName() {
        MultipartFile empty = new MockMultipartFile("file", "a.bin", null, new byte[] {1});
        assertThatThrownBy(() -> scrollService.create("bob", " ", "n", empty))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_persists() throws Exception {
        when(scrollRepository.existsByScrollId("new-id")).thenReturn(false);
        when(scrollRepository.existsByName("New Name")).thenReturn(false);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(uploader));
        MultipartFile file = new MockMultipartFile("file", "doc.txt", "text/plain", "data".getBytes(StandardCharsets.UTF_8));
        when(scrollRepository.save(any(Scroll.class))).thenAnswer(inv -> inv.getArgument(0));

        ScrollSummaryResponse sum = scrollService.create("bob", "new-id", "New Name", file);

        assertThat(sum.getScrollId()).isEqualTo("new-id");
        verify(scrollRepository).save(any(Scroll.class));
    }

    @Test
    void update_rejectsNonOwner() {
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        assertThatThrownBy(() -> scrollService.update(10L, "other", null, null, null))
                .hasMessageContaining("only edit");
    }

    @Test
    void update_ownerChangesName() {
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));
        when(scrollRepository.existsByNameAndIdNot("Renamed", 10L)).thenReturn(false);

        ScrollSummaryResponse sum = scrollService.update(10L, "bob", null, "Renamed", null);

        assertThat(sum.getName()).isEqualTo("Renamed");
    }

    @Test
    void delete_removesWhenOwner() {
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        scrollService.delete(10L, "bob");

        verify(scrollRepository).delete(scroll);
    }

    @Test
    void preview_notFound() {
        when(scrollRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scrollService.preview(99L)).hasMessageContaining("Scroll not found");
    }

    @Test
    void preview_nullMime_fallsThroughToHex() {
        scroll.setData(new byte[] {1});
        scroll.setMimeType(null);
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        assertThat(scrollService.preview(10L).getPreviewKind()).isEqualTo("HEX");
    }

    @Test
    void preview_applicationJson_branch() {
        scroll.setData("{}".getBytes(StandardCharsets.UTF_8));
        scroll.setMimeType("application/json");
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        ScrollPreviewResponse p = scrollService.preview(10L);

        assertThat(p.getPreviewKind()).isEqualTo("TEXT");
        assertThat(p.getPreviewContent()).isEqualTo("{}");
    }

    @Test
    void preview_text_noTruncateWhenShort() {
        scroll.setData("hi".getBytes(StandardCharsets.UTF_8));
        scroll.setMimeType("text/plain");
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        assertThat(scrollService.preview(10L).getPreviewContent()).isEqualTo("hi");
    }

    @Test
    void preview_hex_noTruncateLineWhenSmall() {
        scroll.setData(new byte[] {1, 2});
        scroll.setMimeType("application/pdf");
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        ScrollPreviewResponse p = scrollService.preview(10L);
        assertThat(p.getPreviewKind()).isEqualTo("HEX");
        assertThat(p.getPreviewContent()).doesNotContain("truncated");
    }

    @Test
    void download_mimeNull_usesOctetStream() {
        scroll.setMimeType(null);
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        ScrollFilePayload f = scrollService.download(10L, "bob");

        assertThat(f.getMimeType()).isEqualTo("application/octet-stream");
    }

    @Test
    void create_duplicateScrollId() {
        MultipartFile file = new MockMultipartFile("file", "a.txt", null, new byte[] {1});
        when(scrollRepository.existsByScrollId("dup")).thenReturn(true);

        assertThatThrownBy(() -> scrollService.create("bob", "dup", "Name", file))
                .hasMessageContaining("Scroll ID already");
    }

    @Test
    void create_duplicateName() {
        MultipartFile file = new MockMultipartFile("file", "a.txt", null, new byte[] {1});
        when(scrollRepository.existsByScrollId("id1")).thenReturn(false);
        when(scrollRepository.existsByName("DupName")).thenReturn(true);

        assertThatThrownBy(() -> scrollService.create("bob", "id1", "DupName", file))
                .hasMessageContaining("Scroll name already");
    }

    @Test
    void create_emptyFile() {
        MultipartFile file = new MockMultipartFile("file", "a.txt", null, new byte[0]);

        assertThatThrownBy(() -> scrollService.create("bob", "id1", "N", file)).hasMessageContaining("File");
    }

    @Test
    void create_nullScrollId() {
        MultipartFile file = new MockMultipartFile("file", "a.txt", null, new byte[] {1});

        assertThatThrownBy(() -> scrollService.create("bob", null, "N", file))
                .hasMessageContaining("Scroll ID and name");
    }

    @Test
    void create_uploaderNotFound() {
        when(scrollRepository.existsByScrollId("id1")).thenReturn(false);
        when(scrollRepository.existsByName("N")).thenReturn(false);
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());
        MultipartFile file = new MockMultipartFile("file", "a.txt", null, new byte[] {1});

        assertThatThrownBy(() -> scrollService.create("nobody", "id1", "N", file))
                .hasMessageContaining("User not found");
    }

    @Test
    void create_getBytesFailure() throws Exception {
        when(scrollRepository.existsByScrollId("id1")).thenReturn(false);
        when(scrollRepository.existsByName("N")).thenReturn(false);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(uploader));
        MultipartFile bad = mock(MultipartFile.class);
        when(bad.isEmpty()).thenReturn(false);
        when(bad.getBytes()).thenThrow(new RuntimeException("io"));

        assertThatThrownBy(() -> scrollService.create("bob", "id1", "N", bad))
                .hasMessageContaining("Could not read file");
    }

    @Test
    void update_scrollNotFound() {
        when(scrollRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scrollService.update(99L, "bob", null, null, null))
                .hasMessageContaining("Scroll not found");
    }

    @Test
    void update_scrollIdConflict() {
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));
        when(scrollRepository.existsByScrollIdAndIdNot("taken", 10L)).thenReturn(true);

        assertThatThrownBy(() -> scrollService.update(10L, "bob", "taken", null, null))
                .hasMessageContaining("Scroll ID already");
    }

    @Test
    void update_nameConflict() {
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));
        when(scrollRepository.existsByNameAndIdNot("TakenName", 10L)).thenReturn(true);

        assertThatThrownBy(() -> scrollService.update(10L, "bob", null, "TakenName", null))
                .hasMessageContaining("Scroll name already");
    }

    @Test
    void update_sameScrollId_skipsExistsCheck() {
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        scrollService.update(10L, "bob", "sid-1", null, null);

        verify(scrollRepository, never()).existsByScrollIdAndIdNot(any(), any());
    }

    @Test
    void update_withFile_incrementsUploadEventCount() throws Exception {
        scroll.setUploadEventCount(1);
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));
        MultipartFile f = new MockMultipartFile("file", "b.bin", "application/pdf", new byte[] {9});

        ScrollSummaryResponse sum = scrollService.update(10L, "bob", null, null, f);

        assertThat(sum.getUploadEventCount()).isEqualTo(2);
    }

    @Test
    void update_fileReadFails() throws Exception {
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));
        MultipartFile bad = mock(MultipartFile.class);
        when(bad.isEmpty()).thenReturn(false);
        when(bad.getBytes()).thenThrow(new RuntimeException("x"));

        assertThatThrownBy(() -> scrollService.update(10L, "bob", null, null, bad))
                .hasMessageContaining("Could not read file");
    }

    @Test
    void delete_notFound() {
        when(scrollRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scrollService.delete(99L, "bob")).hasMessageContaining("Scroll not found");
    }

    @Test
    void delete_wrongOwner() {
        when(scrollRepository.findById(10L)).thenReturn(Optional.of(scroll));

        assertThatThrownBy(() -> scrollService.delete(10L, "intruder")).hasMessageContaining("only remove");
    }

    @Test
    void toSummary_usesUsernameWhenFullNameNull() {
        uploader.setFullName(null);
        when(scrollRepository.findAll(any(Specification.class))).thenReturn(List.of(scroll));

        ScrollSummaryResponse row = scrollService.search(null, null, null, null, null).get(0);

        assertThat(row.getUploaderFullName()).isEqualTo("bob");
    }
}
