package newnop.taskmanager.service.impl;

import newnop.taskmanager.dto.UserDto;
import newnop.taskmanager.entity.Role;
import newnop.taskmanager.entity.User;
import newnop.taskmanager.exception.ResourceNotFoundException;
import newnop.taskmanager.repository.RoleRepository;
import newnop.taskmanager.repository.UserRepository;
import newnop.taskmanager.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

    @Override
    public UserDto getUserById(UUID uuid) {
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        if (user.getRole() == null) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default Role ROLE_USER not found"));
            user.setRole(userRole);
        }
        
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto updateUser(UUID uuid, UserDto userDto) {
        User existingUser = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));

        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(UUID uuid) {
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuid));
        userRepository.delete(user);
    }

    @Override
    public UserDto assignRoleToUser(UUID userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        user.setRole(role);
        User updatedUser = userRepository.save(user);
        return modelMapper.map(updatedUser, UserDto.class);
    }
}
