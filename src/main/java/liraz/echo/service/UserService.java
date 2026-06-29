package liraz.echo.service;

import liraz.echo.domain.user.Role;
import liraz.echo.domain.user.User;
import liraz.echo.exceptions.ResourceNotFoundException;
import liraz.echo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User require(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public boolean usernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean usernameTakenByOther(String username, Long id) {
        return userRepository.existsByUsernameAndIdNot(username, id);
    }

    @Transactional(readOnly = true)
    public User requireByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public User register(String username, String rawPassword, String name, Role role) {
        return userRepository.save(
                new User(username, passwordEncoder.encode(rawPassword), name, role));
    }

    public User update(Long id, String username, String rawPassword, String name, Role role) {
        User user = require(id);
        user.setUsername(username);
        user.setName(name);
        user.setRole(role);
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}