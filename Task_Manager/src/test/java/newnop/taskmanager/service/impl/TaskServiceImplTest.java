package newnop.taskmanager.service.impl;

import newnop.taskmanager.constant.AppConstants;
import newnop.taskmanager.dto.TaskDto;
import newnop.taskmanager.entity.Task;
import newnop.taskmanager.entity.TaskStatus;
import newnop.taskmanager.entity.User;
import newnop.taskmanager.exception.ResourceNotFoundException;
import newnop.taskmanager.repository.TaskRepository;
import newnop.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID taskUuid;
    private UUID userUuid;
    private Task task;
    private TaskDto taskDto;
    private User user;

    @BeforeEach
    void setUp() {
        taskUuid = UUID.randomUUID();
        userUuid = UUID.randomUUID();

        user = new User();
        user.setUuid(userUuid);
        user.setUsername("testuser");

        task = new Task();
        task.setUuid(taskUuid);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.PENDING);
        task.setOwner(user);

        taskDto = new TaskDto();
        taskDto.setUuid(taskUuid);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(TaskStatus.PENDING);
        taskDto.setOwnerUuid(userUuid);
    }

    private Authentication createAuthentication(String uuid, String role) {
        return new UsernamePasswordAuthenticationToken(
                uuid,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    void getTaskById_Success_AsOwner() {
        Authentication auth = createAuthentication(userUuid.toString(), AppConstants.ROLE_USER);

        when(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task));
        when(modelMapper.map(task, TaskDto.class)).thenReturn(taskDto);

        TaskDto result = taskService.getTaskById(taskUuid, auth);

        assertNotNull(result);
        assertEquals(taskUuid, result.getUuid());
        assertEquals(userUuid, result.getOwnerUuid());
        verify(taskRepository, times(1)).findById(taskUuid);
    }

    @Test
    void getTaskById_Success_AsAdmin() {
        Authentication adminAuth = createAuthentication(UUID.randomUUID().toString(), AppConstants.ROLE_ADMIN);

        when(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task));
        when(modelMapper.map(task, TaskDto.class)).thenReturn(taskDto);

        TaskDto result = taskService.getTaskById(taskUuid, adminAuth);

        assertNotNull(result);
        verify(taskRepository, times(1)).findById(taskUuid);
    }

    @Test
    void getTaskById_AccessDenied() {
        Authentication otherUserAuth = createAuthentication(UUID.randomUUID().toString(), AppConstants.ROLE_USER);

        when(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(taskUuid, otherUserAuth));
        verify(taskRepository, times(1)).findById(taskUuid);
    }

    @Test
    void createTask_Success() {
        when(modelMapper.map(taskDto, Task.class)).thenReturn(task);
        when(userRepository.getReferenceById(userUuid)).thenReturn(user);
        when(taskRepository.save(task)).thenReturn(task);
        when(modelMapper.map(task, TaskDto.class)).thenReturn(taskDto);

        TaskDto result = taskService.createTask(taskDto, userUuid);

        assertNotNull(result);
        assertEquals(userUuid, result.getOwnerUuid());
        verify(taskRepository, times(1)).save(task);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/tasks"), any(TaskDto.class));
    }

    @Test
    void updateTask_Success_AsOwner() {
        Authentication auth = createAuthentication(userUuid.toString(), AppConstants.ROLE_USER);

        when(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(modelMapper.map(task, TaskDto.class)).thenReturn(taskDto);

        TaskDto result = taskService.updateTask(taskUuid, taskDto, auth);

        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/tasks"), any(TaskDto.class));
    }

    @Test
    void deleteTask_Success_AsOwner() {
        Authentication auth = createAuthentication(userUuid.toString(), AppConstants.ROLE_USER);

        when(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskUuid, auth);

        verify(taskRepository, times(1)).delete(task);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/tasks/deleted"), eq(taskUuid.toString()));
    }

    @Test
    void deleteTask_AccessDenied() {
        Authentication otherUserAuth = createAuthentication(UUID.randomUUID().toString(), AppConstants.ROLE_USER);

        when(taskRepository.findById(taskUuid)).thenReturn(Optional.of(task));

        assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(taskUuid, otherUserAuth));
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
