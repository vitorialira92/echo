package liraz.echo.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import liraz.echo.domain.user.Role;

public record UserUpdateRequest(
        @NotBlank @Size(max = 50) String username,
        @Size(min = 4, max = 100) String password,
        @NotBlank @Size(max = 120) String name,
        @NotNull Role role
) {
}
