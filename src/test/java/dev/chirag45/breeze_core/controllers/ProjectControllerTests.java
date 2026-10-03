package dev.chirag45.breeze_core.controllers;

import dev.chirag45.breeze_core.dto.request.ProjectUpsertRequest;
import dev.chirag45.breeze_core.entities.UserEntity;
import dev.chirag45.breeze_core.repository.UserRepository;
import dev.chirag45.breeze_core.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjectControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createsProjectForVerifiedClerkSubject() throws Exception {
        UserEntity owner = userRepository.saveAndFlush(new UserEntity("project_http_owner"));

        mockMvc.perform(post("/api/projects")
                        .with(jwt().jwt(token -> token.subject(owner.getClerkUserId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sample API\",\"description\":\"A backend project\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.data.ownerUserId").value(owner.getId().toString()))
                .andExpect(jsonPath("$.data.name").value("Sample API"))
                .andExpect(jsonPath("$.data.description").value("A backend project"))
                .andExpect(jsonPath("$.data.workSpaceInitializedOn").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void rejectsInvalidProjectName() throws Exception {
        UserEntity owner = userRepository.saveAndFlush(new UserEntity("project_http_validation"));

        mockMvc.perform(post("/api/projects")
                        .with(jwt().jwt(token -> token.subject(owner.getClerkUserId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \",\"description\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    void doesNotRevealAnotherUsersProject() throws Exception {
        UserEntity owner = userRepository.saveAndFlush(new UserEntity("project_http_private_owner"));
        UserEntity other = userRepository.saveAndFlush(new UserEntity("project_http_other"));
        UUID projectId = projectService.create(
                owner.getClerkUserId(),
                new ProjectUpsertRequest("Private project", null)
        ).id();

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .with(jwt().jwt(token -> token.subject(other.getClerkUserId()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PROJECT_NOT_FOUND"));
    }
}
