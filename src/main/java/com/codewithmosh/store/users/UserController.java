package com.codewithmosh.store.users;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @GetMapping
    public List<UserDto> getAllUsers(@RequestHeader(name = "x-auth-token", required = false) String authToken, @RequestParam(name = "sort", required = false, defaultValue = "") String sortBy) {


        System.out.println(authToken);

        if (!Set.of("name", "email").contains(sortBy)) {
            sortBy = "name";

        }

        return userRepository.findAll(Sort.by(sortBy).ascending()).stream().map(userMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
            //            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok(userMapper.toDto(user));
        }
    }


    @PostMapping
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterUserRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        // 409 Conflict
        if (userRepository.existsByEmail(request.getEmail()))
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("email", "Email is already registered"));

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user = userRepository.save(user);

        var uri = uriBuilder.path("/users/{id}").buildAndExpand(user.getId()).toUri();


        return ResponseEntity.created(uri).body(userMapper.toDto(user));
    }


    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable(name = "id") Long id, @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        } else {
            userMapper.update(request, user);
            userRepository.save(user);

            return ResponseEntity.ok(userMapper.toDto(user));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable(name = "id") Long id, @RequestBody ChangePasswordRequest request) {
        {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            } else {
                if (!request.getOldPassword().equals(user.getPassword())) {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

                } else {
                    user.setPassword(request.getNewPassword());
                    userRepository.save(user);

                    return ResponseEntity.ok().build();
                }
            }

        }
    }

}
