package dalgrock.playlist.controller;

import dalgrock.playlist.core.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {

    private final JwtTokenProvider tokenProvider;

    @GetMapping("/token")
    public String generateToken() {
        String token = tokenProvider.createAccessToken(1L, "USER");
        return token;
    }
}
