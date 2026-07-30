package dts.com.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Class hỗ trợ giải mã và xác thực JWT Access Token tại API Gateway.
 */
@Component
public class JwtUtil {

    // Secret Key dùng chung với Identity Service (cấu hình trong application.yaml)
    @Value("${jwt.secret:dts_microservices_secret_key_for_jwt_verification_2026_super_secure_key_384_bits}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Trích xuất các thuộc tính (Claims) từ JWT Token
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Kiểm tra xem Token đã hết hạn chưa
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Kiểm tra tính hợp lệ của Token (chữ ký + hạn dùng)
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserId(Claims claims) {
        return claims.getSubject();
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        return claims.get("roles", List.class);
    }
}
