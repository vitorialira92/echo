package liraz.echo.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import liraz.echo.domain.user.Role;

public record UserCreateRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(min = 4, max = 100) String password,
        @NotBlank @Size(max = 120) String name,
        Role role
) { }
