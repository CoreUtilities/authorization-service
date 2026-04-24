package com.authorization_service;

import com.authorization_service.entity.User;
import com.authorization_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OAuth2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testOAuth2AuthorizationEndpointExists() throws Exception {
        mockMvc.perform(post("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    public void testUserCanAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(post("/api/protected"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGoogleUserCanBeCreated() {
        User googleUser = new User();
        googleUser.setEmail("test@gmail.com");
        googleUser.setUsername("test@gmail.com");
        googleUser.setGoogleId("123456789");
        googleUser.setAuthProvider("google");
        googleUser.setPassword(null);

        User savedUser = userRepository.save(googleUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getGoogleId()).isEqualTo("123456789");
        assertThat(savedUser.getAuthProvider()).isEqualTo("google");
    }

    @Test
    public void testFindUserByGoogleId() {
        User googleUser = new User();
        googleUser.setEmail("test2@gmail.com");
        googleUser.setUsername("test2@gmail.com");
        googleUser.setGoogleId("987654321");
        googleUser.setAuthProvider("google");
        googleUser.setPassword(null);

        userRepository.save(googleUser);

        var foundUser = userRepository.findByGoogleId("987654321");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test2@gmail.com");
    }

    @Test
    public void testFindUserByEmail() {
        User googleUser = new User();
        googleUser.setEmail("search@gmail.com");
        googleUser.setUsername("search@gmail.com");
        googleUser.setGoogleId("111111111");
        googleUser.setAuthProvider("google");
        googleUser.setPassword(null);

        userRepository.save(googleUser);

        var foundUser = userRepository.findByEmail("search@gmail.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getGoogleId()).isEqualTo("111111111");
    }
}
