package com.blackcode.auth_service.security.jwt;

import com.blackcode.auth_service.exception.InvalidJwtException;
import com.blackcode.auth_service.exception.TokenExpiredException;
import com.blackcode.auth_service.security.service.UserAuthDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${blackcode.app.jwtSecret}")
    private String jwtSecret;

    @Value("${blackcode.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtTokenUserAuth(UserAuthDetailsImpl userAuthPrincipal){
        return generateTokenFromUsername(userAuthPrincipal.getUsername());
    }

    private Key getSigningKey(){
        return new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateTokenFromUsername(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public void assertValidToken(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

        }catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired", e);
        } catch (JwtException e) {
            throw new InvalidJwtException("Invalid JWT token", e);
        } catch (IllegalArgumentException e) {
            throw new InvalidJwtException("Token is null or empty", e);
        }
    }

}
