package liraz.echo.dto.user;

import liraz.echo.domain.user.Role;
import liraz.echo.domain.user.User;

public record UserResponse(
        Long id,
        String username,
        String name,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole()
        );
    }
}
