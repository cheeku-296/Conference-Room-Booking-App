package com.conference.booking.service;

import com.conference.booking.entity.Role;
import com.conference.booking.entity.User;
import com.conference.booking.dto.SignupRequest;
import com.conference.booking.repository.RoleRepository;
import com.conference.booking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(SignupRequest signUpRequest) {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (signUpRequest.getRoles() != null && !signUpRequest.getRoles().isEmpty()) {
            for (String roleName : signUpRequest.getRoles()) {
                switch (roleName.toUpperCase()) {
                    case "ROLE_ADMIN":
                        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Admin role not found"));
                        roles.add(adminRole);
                        break;
                    case "ROLE_USER":
                        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("User role not found"));
                        roles.add(userRole);
                        break;
                    default:
                        throw new RuntimeException("Role not recognized: " + roleName);
                }
            }
        } else {
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("User role not found"));
            roles.add(userRole);
        }
    
        user.setRoles(roles);
        return userRepository.save(user);
    }
}