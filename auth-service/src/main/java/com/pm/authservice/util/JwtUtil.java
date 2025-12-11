package com.pm.authservice.util;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;

    public JwtUtil(@Value("${jwt.secret") String secret) {
        byte[] keyBytes = null;
        // DIAGNOSTIC CODE ONLY:
        String rawEnvVar = System.getenv("JWT_SECRET");
        System.out.println("JWT_SECRET to check it is reaching the : " + rawEnvVar);
        try {
            System.out.println("Value of raw secret key is " + secret);
            keyBytes = Base64.getDecoder().decode(rawEnvVar.getBytes(
                    StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            System.out.println("Decoding failed despite sanitization.");
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);

    }

    public static byte[] decodeSanitizedBase64(byte[] inputBytes){
        // Converting the byte array to a String using US-ASCII
        String secretWithHiddenChar = new String(inputBytes, StandardCharsets.US_ASCII);

        // Filter the string: Remove all characters that are not part of the Base64 alphabet.
        // This targets control characters (like ASCII 24), spaces, and any other junk.
//        System.out.println("Before cleaning via replaceAll() " + secretWithHiddenChar);
        String cleanSecret =  secretWithHiddenChar.replaceAll("[^A-za-z0-9+/=]]", "");
//        System.out.println("Decoding sanitized base64 string before Strip and trim : " + cleanSecret);
        //Performing optional trimming
        cleanSecret = cleanSecret.trim().strip();
        System.out.println("Decoding sanitized base64 string: " + cleanSecret);
        return cleanSecret.getBytes(StandardCharsets.UTF_8);


    }

    public String generateToken(String email, String role){
        return Jwts.builder()
                .subject(email)
                .claim("role",role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+ 1000*60*60*10))
                .signWith(secretKey)
                .compact();
    }

}
