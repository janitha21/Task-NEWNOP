package newnop.taskmanager.service.impl;

import newnop.taskmanager.dto.TaskDto;
import newnop.taskmanager.entity.Task;
import newnop.taskmanager.exception.ResourceNotFoundException;
import newnop.taskmanager.repository.TaskRepository;
import newnop.taskmanager.service.TaskService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import newnop.taskmanager.entity.TaskStatus;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, ModelMapper modelMapper, org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.taskRepository = taskRepository;
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
    public TaskDto getTaskById(UUID uuid) {
        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with UUID: " + uuid));
        TaskDto dto = modelMapper.map(task, TaskDto.class);
        if (task.getOwner() != null) {
            dto.setOwnerUuid(task.getOwner().getUuid());
        }
        return dto;
    }

    @Override
    public TaskDto createTask(TaskDto taskDto, UUID ownerUuid) {
        Task task = modelMapper.map(taskDto, Task.class);
        
        newnop.taskmanager.entity.User owner = new newnop.taskmanager.entity.User();
        owner.setUuid(ownerUuid);
        task.setOwner(owner);
        
        Task savedTask = taskRepository.save(task);
        TaskDto dto = modelMapper.map(savedTask, TaskDto.class);
        dto.setOwnerUuid(ownerUuid);

        messagingTemplate.convertAndSend("/topic/tasks", dto);
        
        return dto;
    }

    @Override
    public TaskDto updateTask(UUID uuid, TaskDto taskDto, UUID requesterUuid, boolean isAdmin) {
        Task existingTask = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with UUID: " + uuid));
        
        if (!isAdmin && (existingTask.getOwner() == null || !existingTask.getOwner().getUuid().equals(requesterUuid))) {
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
    public void deleteTask(UUID uuid, UUID requesterUuid, boolean isAdmin) {
        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with UUID: " + uuid));
                
        if (!isAdmin && (task.getOwner() == null || !task.getOwner().getUuid().equals(requesterUuid))) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this task");
        }
        
        taskRepository.delete(task);
        
        // Broadcast the deletion event (you could send a specific DTO or just the UUID)
        messagingTemplate.convertAndSend("/topic/tasks/deleted", uuid.toString());
    }
}
