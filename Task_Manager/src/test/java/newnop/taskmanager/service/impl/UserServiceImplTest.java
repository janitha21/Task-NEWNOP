package newnop.taskmanager.service.impl;

import newnop.taskmanager.constant.AppConstants;
import newnop.taskmanager.dto.UserDto;
import newnop.taskmanager.entity.Role;
import newnop.taskmanager.entity.User;
import newnop.taskmanager.repository.RoleRepository;
import newnop.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        UUID userUuid = UUID.randomUUID();
        
        userDto = new UserDto();
        userDto.setUuid(userUuid);
        userDto.setUsername("testuser");
        userDto.setPassword("password123");
        userDto.setEmail("test@test.com");
        
        user = new User();
        user.setUuid(userUuid);
        user.setUsername("testuser");
        user.setPassword("encoded_password");
        user.setEmail("test@test.com");
        
        role = new Role();
        role.setId(1L);
        role.setName(AppConstants.ROLE_USER);
    }

    @Test
    void createUser_Success_WithDefaultRole() {
        when(modelMapper.map(userDto, User.class)).thenReturn(user);
        when(passwordEncoder.encode("encoded_password")).thenReturn("encoded_password");
        when(roleRepository.findByName(AppConstants.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        
        verify(passwordEncoder, times(1)).encode("encoded_password");
        verify(roleRepository, times(1)).findByName(AppConstants.ROLE_USER);
        verify(userRepository, times(1)).save(user);
    }
}
