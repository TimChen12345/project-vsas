package com.vsas.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.vsas.entity.Role;
import com.vsas.entity.Scroll;
import com.vsas.entity.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ScrollSpecificationsTest {

    @Autowired private ScrollRepository scrollRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void filtered_matchesUploaderAndScrollId() {
        User u = persistUser("u1", "key-a");
        persistScroll(u, "S1", "Alpha Scroll");
        persistScroll(u, "S2", "Beta Scroll");

        List<Scroll> byKey =
                scrollRepository.findAll(ScrollSpecifications.filtered("key-a", null, null, null, null));
        assertThat(byKey).hasSize(2);

        List<Scroll> bySid =
                scrollRepository.findAll(ScrollSpecifications.filtered(null, "S1", null, null, null));
        assertThat(bySid).hasSize(1);
        assertThat(bySid.get(0).getName()).isEqualTo("Alpha Scroll");
    }

    @Test
    void filtered_nameSubstringCaseInsensitive() {
        User u = persistUser("u2", "key-b");
        persistScroll(u, "X1", "Hello World");

        List<Scroll> rows =
                scrollRepository.findAll(ScrollSpecifications.filtered(null, null, "world", null, null));
        assertThat(rows).hasSize(1);
    }

    @Test
    void filtered_uploadedRange() {
        User u = persistUser("u3", "key-c");
        Scroll s = persistScroll(u, "R1", "Ranged");
        Instant from = s.getUploadedAt().minusSeconds(60);
        Instant to = s.getUploadedAt().plusSeconds(60);

        List<Scroll> rows =
                scrollRepository.findAll(ScrollSpecifications.filtered(null, null, null, from, to));
        assertThat(rows).extracting(Scroll::getScrollId).containsExactly("R1");
    }

    @Test
    void filtered_nameOnlyWildcards_addsNoNamePredicate_returnsAllScrolls() {
        persistScroll(persistUser("u4", "key-d"), "W1", "One");
        persistScroll(persistUser("u5", "key-e"), "W2", "Two");

        List<Scroll> rows =
                scrollRepository.findAll(ScrollSpecifications.filtered(null, null, "%__", null, null));

        assertThat(rows).hasSize(2);
    }

    @Test
    void filtered_uploadedFromOnly() {
        User u = persistUser("u6", "key-f");
        Scroll s = persistScroll(u, "F1", "FromOnly");
        Instant from = s.getUploadedAt().minusSeconds(1);

        List<Scroll> rows =
                scrollRepository.findAll(ScrollSpecifications.filtered(null, null, null, from, null));

        assertThat(rows).extracting(Scroll::getScrollId).contains("F1");
    }

    @Test
    void filtered_uploadedToOnly() {
        User u = persistUser("u7", "key-g");
        Scroll s = persistScroll(u, "T1", "ToOnly");
        Instant to = s.getUploadedAt().plusSeconds(3600);

        List<Scroll> rows =
                scrollRepository.findAll(ScrollSpecifications.filtered(null, null, null, null, to));

        assertThat(rows).extracting(Scroll::getScrollId).contains("T1");
    }

    private User persistUser(String username, String idKey) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("ENC");
        u.setIdKey(idKey);
        u.setFullName(username);
        u.setEmail(username + "@t.com");
        u.setPhoneNumber("+10000000000");
        u.setRole(Role.USER);
        return userRepository.save(u);
    }

    private Scroll persistScroll(User uploader, String scrollId, String name) {
        Scroll s = new Scroll();
        s.setScrollId(scrollId);
        s.setName(name);
        s.setUploader(uploader);
        s.setData(new byte[] {1});
        s.setOriginalFilename("f.bin");
        s.setMimeType("application/octet-stream");
        return scrollRepository.save(s);
    }
}
