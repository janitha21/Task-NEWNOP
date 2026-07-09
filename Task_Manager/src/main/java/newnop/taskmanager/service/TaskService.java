package newnop.taskmanager.service;

import newnop.taskmanager.dto.TaskDto;
import newnop.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface TaskService {
    Page<TaskDto> getAllTasks(TaskStatus status, UUID ownerUuid, Pageable pageable);
    TaskDto getTaskById(UUID uuid, Authentication authentication);
    TaskDto createTask(TaskDto taskDto, UUID ownerUuid);
    TaskDto updateTask(UUID uuid, TaskDto taskDto, Authentication authentication);
    void deleteTask(UUID uuid, Authentication authentication);
}
