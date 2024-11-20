package ru.taskmanagment.config.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import ru.taskmanagment.payload.rs.UserAuth;
import ru.taskmanagment.util.AppProperties;
import ru.taskmanagment.util.Constant;


import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenUtil {
    private final SecretKey secretKey;

    public JwtTokenUtil(AppProperties appProperties) {
        byte[] base64EncodeKey = Base64.getEncoder().encode(appProperties.getPlainSecretKey().getBytes());
        this.secretKey = Keys.hmacShaKeyFor(base64EncodeKey);
    }

    public String generateAccessToken(UserAuth userAuth) {
        return Jwts.builder()
                .setSubject(userAuth.getEmail())
                .setIssuer("@luthfipun")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + Constant.JWT_TOKEN_EXPIRED))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid or expired JWT token", e);
        }
    }


}