package ma.smartflow.authserver.srevices;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import ma.smartflow.authserver.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

   public String generateToken(User user){
       return Jwts.builder()
               .subject(user.getUsername())
               .claims(Map.of("role", user.getRole().name(), "userId", user.getId()))
               .signWith(getSigningKey())
               .issuedAt(new Date())
               .expiration(new Date(System.currentTimeMillis() + expiration))
               .compact();
   }

   public String extractUsername(String token){
       return extractClaims(token).getSubject();
   }

   public boolean isTokenValid(String token){
       try {
           extractClaims(token);
           return true;
       }catch (JwtException e){
           return false;
       }
   }



   private Claims extractClaims(String token){
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
