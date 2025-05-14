package org.example.server.service;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.UserDto;
import org.example.server.model.User;
import org.example.server.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not logged in"));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    public List<UserDto> findUserByUsernameContainingIgnoreCase(String username) {
        var user = getCurrentUser();
        return userRepository.findUserByUsernameContainingIgnoreCaseAndIdNot(username, user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<User> findByUsernames(List<String> usernames) {
        return userRepository.findUserByUsernamesCaseSensitive(usernames);
    }
}
