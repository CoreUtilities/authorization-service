package com.authorization_service.security;

import com.authorization_service.entity.User;
import com.authorization_service.repository.UserRepository;
import com.authorization_service.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final com.authorization_service.service.RefreshTokenService refreshTokenService;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtService jwtService, com.authorization_service.service.RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub"); // Google's unique ID
        
        // Find or create user
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGoogleId(googleId);
                    newUser.setEmail(email);
                    newUser.setUsername(email); // Use email as username for Google users
                    newUser.setAuthProvider("google");
                    newUser.setPassword(null); // Google users don't have local passwords
                    return userRepository.save(newUser);
                });
        
        // Generate tokens
        String accessToken = jwtService.generateToken(user.getUsername());
        var refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        
        // Set refresh token as HTTPOnly cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Lax")
                .build();
        
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        
        // Redirect to frontend with access token
        String redirectUrl = String.format("http://localhost:3000/oauth-callback?accessToken=%s", accessToken);
        response.sendRedirect(redirectUrl);
    }
}
