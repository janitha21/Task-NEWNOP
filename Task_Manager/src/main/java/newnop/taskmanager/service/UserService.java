package newnop.taskmanager.service;

import newnop.taskmanager.dto.UserDto;
import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(UUID uuid);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(UUID uuid, UserDto userDto);
    void deleteUser(UUID uuid);
    UserDto assignRoleToUser(UUID userId, Long roleId);
}
