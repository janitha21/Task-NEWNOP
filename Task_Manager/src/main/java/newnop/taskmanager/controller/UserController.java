package newnop.taskmanager.controller;

import newnop.taskmanager.dto.UserDto;
import newnop.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //-----create user
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    //------get all users
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    //-------get user by uuid
    @GetMapping("/{uuid}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID uuid) {
        UserDto userDto = userService.getUserById(uuid);
        return ResponseEntity.ok(userDto);
    }

    //----update user
    @PutMapping("/{uuid}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID uuid, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(uuid, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID uuid) {
        userService.deleteUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uuid}/roles/{roleId}")
    public ResponseEntity<UserDto> assignRoleToUser(@PathVariable UUID uuid, @PathVariable Long roleId) {
        UserDto updatedUser = userService.assignRoleToUser(uuid, roleId);
        return ResponseEntity.ok(updatedUser);
    }
}
