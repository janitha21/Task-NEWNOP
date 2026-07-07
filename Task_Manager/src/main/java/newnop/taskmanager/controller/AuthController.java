package newnop.taskmanager.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.validation.Valid;
import newnop.taskmanager.dto.AuthResponse;
import newnop.taskmanager.dto.LoginRequest;
import newnop.taskmanager.dto.RefreshTokenRequest;
import newnop.taskmanager.dto.UserDto;
import newnop.taskmanager.entity.User;
import newnop.taskmanager.repository.UserRepository;
import newnop.taskmanager.service.JwtService;
import newnop.taskmanager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import newnop.taskmanager.constant.AppConstants;

import java.util.UUID;

@RestController
@RequestMapping(AppConstants.AUTH_API)
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserDto userDto) {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        UserDto userDto = userService.getUserById(user.getUuid());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, userDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        DecodedJWT decoded = jwtService.validateToken(request.getRefreshToken());

        if (decoded == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String tokenType = decoded.getClaim("type").asString();
        if (!"refresh".equals(tokenType)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userUuid = UUID.fromString(decoded.getSubject());
        User user = userRepository.findById(userUuid).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        UserDto userDto = userService.getUserById(userUuid);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken, userDto));
    }
}
