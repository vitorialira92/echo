package liraz.echo.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "echo.security.jwt")
public record JwtProperties(String secret, long expiration) { }
