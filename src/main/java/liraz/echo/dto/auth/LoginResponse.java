package liraz.echo.dto.auth;

public record LoginResponse(String token, String tokenType, long expiresIn, String username, String role) { }
