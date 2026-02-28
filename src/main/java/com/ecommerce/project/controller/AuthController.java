package com.ecommerce.project.controller;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Roles;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignUpRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.project.security.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsImpl;
import com.ecommerce.project.repositories.UserRepository;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        }catch(AuthenticationException e){
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad Credentials");
            map.put("status", false);

            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UserInfoResponse response =
                new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signUpUser(@Valid @RequestBody SignUpRequest request){
        if(userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: username is already taken"));
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already taken"));
        }

        User user = new User(request.getUsername(), request.getEmail(), encoder.encode(request.getPassword()));
        Set<String> strRoles = request.getRole();
        Set<Roles> roles = new HashSet<>();

        if(strRoles == null){
            Roles role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException(AppRole.ROLE_USER.name() + " not found."));
            roles.add(role);
        }
        else{
            strRoles.forEach( role -> {
                        switch (role) {
                            case "admin":
                                Roles adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                        .orElseThrow(() -> new RuntimeException(AppRole.ROLE_ADMIN.name() + " not found."));
                                roles.add(adminRole);
                                break;
                            case "seller":
                                Roles sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                        .orElseThrow(() -> new RuntimeException(AppRole.ROLE_SELLER.name() + " not found."));
                                roles.add(sellerRole);
                                break;
                            default:
                                Roles userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                        .orElseThrow(() -> new RuntimeException(AppRole.ROLE_USER.name() + " not found."));
                                roles.add(userRole);
                        }
                    }
            );
        }
        user.setUserRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User Registered successfully"));
    }

    @GetMapping("/username")
    public String getCurrentUsername(Authentication authentication){
        if(authentication != null){
            return authentication.getName();
        }
        return null;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUserDetails(Authentication authentication){
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UserInfoResponse response =
                new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/signout")
    public ResponseEntity<MessageResponse> signoutUser(){
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new MessageResponse("You've been signed out!!!"));
    }
}
