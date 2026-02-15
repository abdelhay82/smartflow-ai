package ma.smartflow.gatewayserver.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;


    public boolean isTokenValid(String token){
        try {
            extractClaims(token);
            return true;
        }catch (JwtException e){
            return false;
        }
    }

    public Claims extractClaims(String token){
        return Jwts.parser()
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    private Key getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

