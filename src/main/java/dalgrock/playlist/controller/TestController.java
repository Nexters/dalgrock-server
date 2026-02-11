package dalgrock.playlist.controller;

import dalgrock.playlist.core.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {

    private static final String COOKIE_NAME = "access_token";

    private final JwtTokenProvider tokenProvider;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @GetMapping("/token")
    public String generateToken(HttpServletResponse response) {
        String token = tokenProvider.createAccessToken(1L, "USER");

        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpirationMs / 1000));
        response.addCookie(cookie);

        return token;
    }
}
