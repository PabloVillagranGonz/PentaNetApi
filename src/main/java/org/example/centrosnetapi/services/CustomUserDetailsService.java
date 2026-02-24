package org.example.centrosnetapi.services;

import lombok.RequiredArgsConstructor;
import org.example.centrosnetapi.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("USER_NOT_FOUND")
                );
    }
}