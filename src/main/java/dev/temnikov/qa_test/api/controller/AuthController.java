package dev.temnikov.qa_test.api.controller;

import dev.temnikov.qa_test.security.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Auth",
        description = """
                Authentication endpoints.
                
                This API supports two authentication mechanisms:
                - HTTP Basic Auth (email + password)
                - Bearer JWT token (obtained from this controller)
                
                In Swagger UI you can use either Basic or Bearer "Authorize" to call protected endpoints.
                """
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @Operation(
            summary = "Login and obtain JWT",
            description = """
                    Authenticates a user using email and password and returns a JWT token.
                    
                    This endpoint itself does NOT require prior authentication.
                    The returned token can be used as Bearer token for subsequent requests.
                    """,
            security = {} // override global security, login is public
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@RequestBody AuthRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenService.generateToken(userDetails);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setExpiresAt(jwtTokenService.extractExpirationEpochSeconds(token));
        return response;
    }

    @Data
    public static class AuthRequest {
        private String email;
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private long expiresAt; // epoch seconds
    }
}
