package newnop.taskmanager.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import newnop.taskmanager.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:defaultSecretKey123!@#}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 1 day in ms
    private long expiration;

    private static final long ACCESS_TOKEN_EXPIRY  = 15L * 60 * 1000;         // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRY = 7L * 24 * 60 * 60 * 1000; // 7 days

    public String generateToken(User user) {
        return generateAccessToken(user);
    }

    public String generateAccessToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withSubject(user.getUuid().toString())
                .withClaim("username", user.getUsername())
                .withClaim("role", user.getRole().getName().toUpperCase())
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .sign(algorithm);
    }

    public String generateRefreshToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withSubject(user.getUuid().toString())
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            return null; // Invalid token
        }
    }
}
