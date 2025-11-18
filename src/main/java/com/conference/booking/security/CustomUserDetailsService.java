package com.conference.booking.security;

import com.conference.booking.entity.User;
import com.conference.booking.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        String loginInput = usernameOrEmail.trim();

        User user = userRepository.findByUsernameIgnoreCase(loginInput)
                .orElseGet(() ->
                        userRepository.findByEmailIgnoreCase(loginInput)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginInput))
                );

        System.out.println("Loaded user: " + user.getUsername() + " with email: " + user.getEmail());
        System.out.println("User roles: " + user.getRoles());

        return UserPrincipal.create(user);
    }
}