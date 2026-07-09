package newnop.taskmanager.service.impl;

import newnop.taskmanager.dto.TaskDto;
import newnop.taskmanager.entity.Task;
import newnop.taskmanager.exception.ResourceNotFoundException;
import newnop.taskmanager.repository.TaskRepository;
import newnop.taskmanager.repository.UserRepository;
import newnop.taskmanager.service.TaskService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import newnop.taskmanager.entity.TaskStatus;
import newnop.taskmanager.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import newnop.taskmanager.constant.AppConstants;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository, ModelMapper modelMapper, SimpMessagingTemplate messagingTemplate) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public Page<TaskDto> getAllTasks(TaskStatus status, UUID ownerUuid, Pageable pageable) {
        Page<Task> tasks;
        if (status != null && ownerUuid != null) {
            tasks = taskRepository.findByStatusAndOwnerUuid(status, ownerUuid, pageable);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status, pageable);
        } else if (ownerUuid != null) {
            tasks = taskRepository.findByOwnerUuid(ownerUuid, pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }
        return tasks.map(task -> {
            TaskDto dto = modelMapper.map(task, TaskDto.class);
            if (task.getOwner() != null) {
                dto.setOwnerUuid(task.getOwner().getUuid());
            }
            return dto;
        });
    }

    @Override
    public TaskDto getTaskById(UUID uuid, Authentication authentication) {
        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with UUID: " + uuid));
                
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstants.ROLE_ADMIN));
        if (!isAdmin && (task.getOwner() == null || !task.getOwner().getUuid().toString().equals(authentication.getName()))) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this task");
        }
        
        TaskDto dto = modelMapper.map(task, TaskDto.class);
        if (task.getOwner() != null) {
            dto.setOwnerUuid(task.getOwner().getUuid());
        }
        return dto;
    }

    @Override
    public TaskDto createTask(TaskDto taskDto, UUID ownerUuid) {
        Task task = modelMapper.map(taskDto, Task.class);
        
        User owner = userRepository.getReferenceById(ownerUuid);
        task.setOwner(owner);
        
        Task savedTask = taskRepository.save(task);
        TaskDto dto = modelMapper.map(savedTask, TaskDto.class);
        dto.setOwnerUuid(ownerUuid);

        messagingTemplate.convertAndSend("/topic/tasks", dto);
        
        return dto;
    }

    @Override
    public TaskDto updateTask(UUID uuid, TaskDto taskDto, Authentication authentication) {
        Task existingTask = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with UUID: " + uuid));
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstants.ROLE_ADMIN));
        String requesterUuid = authentication.getName();

        if (!isAdmin && (existingTask.getOwner() == null || !existingTask.getOwner().getUuid().toString().equals(requesterUuid))) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update this task");
        }
        
        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setDueDate(taskDto.getDueDate());
        
        Task updatedTask = taskRepository.save(existingTask);
        TaskDto dto = modelMapper.map(updatedTask, TaskDto.class);
        if (updatedTask.getOwner() != null) {
            dto.setOwnerUuid(updatedTask.getOwner().getUuid());
        }
        
        // Broadcast the updated task to WebSocket clients
        messagingTemplate.convertAndSend("/topic/tasks", dto);
        
        return dto;
    }

    @Override
    public void deleteTask(UUID uuid, Authentication authentication) {
        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with UUID: " + uuid));
                
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(AppConstants.ROLE_ADMIN));
        String requesterUuid = authentication.getName();

        if (!isAdmin && (task.getOwner() == null || !task.getOwner().getUuid().toString().equals(requesterUuid))) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this task");
        }
        
        taskRepository.delete(task);
        
        // Broadcast the deletion event (you could send a specific DTO or just the UUID)
        messagingTemplate.convertAndSend("/topic/tasks/deleted", uuid.toString());
    }
}
