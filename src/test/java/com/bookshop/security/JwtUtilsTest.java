package com.bookshop.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String secret = "devSecretKey-32bytes-or-longer-to-satisfy-jjwt-requirements!!";
    private final long expirationMs = 3600000; // 1h

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", secret);
        ReflectionTestUtils.setField(jwtUtils, "expirationMs", expirationMs);
    }

    @Test
    void generateToken_validUserDetails_returnsToken() {
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = jwtUtils.generateToken(userDetails);
        
        assertNotNull(token);
        assertEquals("test@example.com", jwtUtils.extractEmail(token));
    }

    @Test
    void isTokenValid_correctUserDetails_returnsTrue() {
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = jwtUtils.generateToken(userDetails);
        
        assertTrue(jwtUtils.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_incorrectUserDetails_returnsFalse() {
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = jwtUtils.generateToken(userDetails);
        
        UserDetails otherUser = new User("other@example.com", "password", Collections.emptyList());
        assertFalse(jwtUtils.isTokenValid(token, otherUser));
    }

    @Test
    void extractEmail_validToken_returnsEmail() {
        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
        String token = jwtUtils.generateToken(userDetails);
        
        assertEquals("test@example.com", jwtUtils.extractEmail(token));
    }
}
