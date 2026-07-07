package liraz.echo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.expirationMillis = properties.expiration();
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", primaryAuthority(userDetails))
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token);
        boolean sameUser = claims.getSubject() != null
                && claims.getSubject().equals(userDetails.getUsername());
        boolean notExpired = claims.getExpiration() != null
                && claims.getExpiration().after(new Date());
        return sameUser && notExpired;
    }

    public long getExpirationSeconds() {
        return expirationMillis / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String primaryAuthority(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }
}
