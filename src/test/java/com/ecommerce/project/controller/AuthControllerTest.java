package com.ecommerce.project.controller;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Roles;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.AuthTokenFilter;
import com.ecommerce.project.security.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignUpRequest;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public AuthenticationManager authenticationManager() {
            return Mockito.mock(AuthenticationManager.class);
        }

        @Bean
        public JwtUtils jwtUtils() {
            return Mockito.mock(JwtUtils.class);
        }

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public RoleRepository roleRepository() {
            return Mockito.mock(RoleRepository.class);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return Mockito.mock(PasswordEncoder.class);
        }

        @Bean
        public AuthTokenFilter authTokenFilter() {
            return Mockito.mock(AuthTokenFilter.class);
        }

        @Bean
        public UserDetailsServiceImpl userDetailsServiceImpl() {
            return Mockito.mock(UserDetailsServiceImpl.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthController authController;

    @Test
    void authenticateUser_successfulLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test");
        request.setPassword("pass");

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl();
        userDetailsImpl.setUsername("test");
        userDetailsImpl.setAuthorities(List.of(
                 new SimpleGrantedAuthority("ROLE_USER")
        ));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetailsImpl, // principal
                null,            // credentials (can be null for testing)
                userDetailsImpl.getAuthorities() // authorities
        );

        ResponseCookie dummyCookie = ResponseCookie.from("jwt", "dummy-token")
                .httpOnly(false)
                .path("/api")
                .build();

        when(jwtUtils.generateJwtCookie(any(UserDetailsImpl.class))).thenReturn(dummyCookie);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, dummyCookie.toString()))
                .andExpect(jsonPath("$.username").value("test"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void authenticateUser_badCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationException("Bad Credentials") {
                });

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Bad Credentials"))
                .andExpect(jsonPath("$.status").value(false));
    }

    @Test
    void signUpUser_successful() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("newuser");
        request.setEmail("new@mail.com");
        request.setPassword("pass12345678");
        request.setRole(Set.of("user"));

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);

        Roles role = new Roles(AppRole.ROLE_USER);
        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("pass")).thenReturn("encodedpass");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User Registered successfully"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals(1, capturedUser.getUserRoles().size());
        assertTrue(capturedUser.getUserRoles().stream()
                .map(Roles::getRoleName)
                .anyMatch(roleName -> roleName == AppRole.ROLE_USER));
    }

    @Test
    void signUpUser_usernameTaken() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("existing");
        request.setEmail("email@mail.com");
        request.setPassword("pass13456789");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: username is already taken"));
    }

    @Test
    void signUpUser_emailTaken() throws Exception {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("user");
        request.setEmail("email@mail.com");
        request.setPassword("pass13456789");

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("email@mail.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already taken"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUsername_returnsUsername() throws Exception {
        UserDetailsImpl userDetailsImpl = new UserDetailsImpl();
        userDetailsImpl.setUsername("testuser");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetailsImpl,
                null
        );

        String response = authController.getCurrentUsername(authentication);
        assertEquals("testuser", response);
    }

    @Test
    void getCurrentUserDetails_returnsUserDetails() throws Exception {

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl();
        userDetailsImpl.setUsername("testuser");
        userDetailsImpl.setAuthorities(List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        ));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetailsImpl,
                null,
                userDetailsImpl.getAuthorities()
        );

        ResponseEntity<?> response = authController.getCurrentUserDetails(authentication);
        UserInfoResponse body = (UserInfoResponse) response.getBody();
        assertEquals("testuser", body.getUsername());
        assertEquals("ROLE_USER", body.getRoles().get(0));
    }

    @Test
    void signoutUser_success() throws Exception {
        ResponseCookie cookie = ResponseCookie.from("dummy", null)
                .path("/api")
                .build();

        when(jwtUtils.getCleanJwtCookie()).thenReturn(cookie);

        mockMvc.perform(get("/api/auth/signout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, cookie.toString()))
                .andExpect(jsonPath("$.message").value("You've been signed out!!!"));
    }
}