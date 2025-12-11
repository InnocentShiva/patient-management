package com.pm.authservice;

import io.jsonwebtoken.io.Encoders;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.jsonwebtoken.Jwts;
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}



//public class AuthServiceApplication {
//    public static void main(String[] args) {
//        byte[] keyBytes = Jwts.SIG.HS256.key().build().getEncoded();
//        String base64Key = Encoders.BASE64.encode(keyBytes);
//        System.out.println(base64Key);
//    }
//}
