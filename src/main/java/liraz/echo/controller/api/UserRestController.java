package liraz.echo.controller.api;

import jakarta.validation.Valid;
import liraz.echo.domain.user.Role;
import liraz.echo.domain.user.User;
import liraz.echo.exceptions.ConflictException;
import liraz.echo.exceptions.ForbiddenException;
import liraz.echo.service.UserService;
import liraz.echo.dto.user.UserCreateRequest;
import liraz.echo.dto.user.UserResponse;
import liraz.echo.dto.user.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserService userService;

    @GetMapping
    public List<UserResponse> list() {
        return userService.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(userService.require(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request,
                                               Authentication authentication) {
        Role role = request.role() != null ? request.role() : Role.USER;
        if (role == Role.ADMIN && !isAdmin(authentication)) {
            throw new ForbiddenException("Only an authenticated administrator can create an ADMIN account.");
        }
        if (userService.usernameTaken(request.username())) {
            throw new ConflictException("That username is already taken.");
        }
        User created = userService.register(request.username(), request.password(), request.name(), role);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.getId()))
                .body(UserResponse.from(created));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        if (userService.usernameTakenByOther(request.username(), id)) {
            throw new ConflictException("That username is already taken.");
        }
        User updated = userService.update(id, request.username(), request.password(), request.name(), request.role());
        return UserResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.require(id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (ROLE_ADMIN.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}