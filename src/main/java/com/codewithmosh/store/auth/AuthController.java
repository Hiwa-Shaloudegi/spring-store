package com.codewithmosh.store.auth;

import com.codewithmosh.store.users.UserDto;
import com.codewithmosh.store.users.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    //    private final UserRepository userRepository;
    //    private final PasswordEncoder passwordEncoder;

    //    @PostMapping("/login")
    //    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
    //        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
    //        if (user == null) {
    //            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
    //        }
    //
    //        boolean matches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
    //        if (!matches) {
    //            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
    //        }
    //
    //        return ResponseEntity.ok().build();
    //    }

    private final JwtService jwtService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletResponse response
    ) {
        JwtResponse jwtResponse = authService.login(request);

        var cookie = new Cookie("refreshToken", jwtResponse.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        cookie.setSecure(true); // only https connections
        response.addCookie(cookie);

        return ResponseEntity.ok(jwtResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        UserDto me = authService.me();
        return ResponseEntity.ok(me);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue(value = "refreshToken") String refreshToken
    ) {
        JwtResponse jwtResponse = authService.refresh(refreshToken);

        return ResponseEntity.ok(jwtResponse);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(JwtExpiredException.class)
    public ResponseEntity<?> handleJwtExpiredException(JwtExpiredException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
    }
}
