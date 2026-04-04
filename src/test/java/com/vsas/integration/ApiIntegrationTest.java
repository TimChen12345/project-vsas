package com.vsas.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void register_login_profile_and_scroll_flow() throws Exception {
        String reg =
                """
                {
                  "username": "alice",
                  "password": "alicepass",
                  "idKey": "alice-id",
                  "fullName": "Alice",
                  "email": "alice@test.com",
                  "phoneNumber": "+61400000000"
                }
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg))
                .andExpect(status().isCreated());

        String loginBody = "{\"username\":\"alice\",\"password\":\"alicepass\"}";
        MvcResult login =
                mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").exists())
                        .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).path("token").asText();

        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));

        MockMultipartFile file =
                new MockMultipartFile("file", "doc.txt", "text/plain", "scroll-bytes".getBytes());
        MvcResult created =
                mockMvc.perform(
                                multipart("/api/scrolls")
                                        .file(file)
                                        .param("scrollId", "scroll-001")
                                        .param("name", "First Scroll")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.scrollId").value("scroll-001"))
                        .andReturn();

        long scrollDbId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(get("/api/scrolls")).andExpect(status().isOk()).andExpect(jsonPath("$[0].scrollId").value("scroll-001"));

        mockMvc.perform(get("/api/scrolls/" + scrollDbId + "/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previewKind").value("TEXT"));

        mockMvc.perform(get("/api/scrolls/" + scrollDbId + "/download"))
                .andExpect(status().isUnauthorized());

        byte[] body =
                mockMvc.perform(
                                get("/api/scrolls/" + scrollDbId + "/download")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray();
        assertThat(body).isEqualTo("scroll-bytes".getBytes());
    }

    @Test
    void register_validationReturnsFieldErrors() throws Exception {
        String bad =
                """
                {
                  "username": "ab",
                  "password": "x",
                  "idKey": "x",
                  "fullName": "",
                  "email": "not-email",
                  "phoneNumber": "bad"
                }
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void admin_endpoints() throws Exception {
        String loginBody = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        MvcResult login =
                mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                        .andExpect(status().isOk())
                        .andReturn();
        String adminToken = objectMapper.readTree(login.getResponse().getContentAsString()).path("token").asText();

        mockMvc.perform(get("/api/admin/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void guest_cannotAccessAdmin() throws Exception {
        String reg =
                """
                {
                  "username": "bob",
                  "password": "bobpass1",
                  "idKey": "bob-id",
                  "fullName": "Bob",
                  "email": "bob@test.com",
                  "phoneNumber": "+61400000001"
                }
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg))
                .andExpect(status().isCreated());
        String loginBody = "{\"username\":\"bob\",\"password\":\"bobpass1\"}";
        MvcResult login =
                mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                        .andExpect(status().isOk())
                        .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).path("token").asText();

        mockMvc.perform(get("/api/admin/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_noContent() throws Exception {
        String reg =
                """
                {
                  "username": "carol",
                  "password": "oldpass1",
                  "idKey": "carol-id",
                  "fullName": "Carol",
                  "email": "carol@test.com",
                  "phoneNumber": "+61400000002"
                }
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg))
                .andExpect(status().isCreated());
        String loginBody = "{\"username\":\"carol\",\"password\":\"oldpass1\"}";
        MvcResult login =
                mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                        .andExpect(status().isOk())
                        .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).path("token").asText();

        String pwBody = "{\"currentPassword\":\"oldpass1\",\"newPassword\":\"newpass1\"}";
        mockMvc.perform(
                        put("/api/users/me/password")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(pwBody))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isUnauthorized());

        String newLogin = "{\"username\":\"carol\",\"password\":\"newpass1\"}";
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(newLogin))
                .andExpect(status().isOk());
    }

    @Test
    void deleteScroll_ownerNoContent() throws Exception {
        String reg =
                """
                {
                  "username": "dave",
                  "password": "davepass1",
                  "idKey": "dave-id",
                  "fullName": "Dave",
                  "email": "dave@test.com",
                  "phoneNumber": "+61400000003"
                }
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(reg))
                .andExpect(status().isCreated());
        String loginBody = "{\"username\":\"dave\",\"password\":\"davepass1\"}";
        MvcResult login =
                mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                        .andExpect(status().isOk())
                        .andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).path("token").asText();

        MockMultipartFile file =
                new MockMultipartFile("file", "d.txt", "text/plain", "x".getBytes());
        MvcResult created =
                mockMvc.perform(
                                multipart("/api/scrolls")
                                        .file(file)
                                        .param("scrollId", "del-me")
                                        .param("name", "To Delete")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                        .andExpect(status().isCreated())
                        .andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(
                        delete("/api/scrolls/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/scrolls")).andExpect(status().isOk());
    }

    @Test
    void homePage_containsVsas() throws Exception {
        String html = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("VSAS");
    }
}
