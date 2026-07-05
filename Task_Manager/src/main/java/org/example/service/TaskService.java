package org.example.service;

import org.example.dto.TaskDto;
import java.util.List;
import java.util.UUID;

public interface TaskService {
    List<TaskDto> getAllTasks();
    TaskDto getTaskById(UUID uuid);
    TaskDto createTask(TaskDto taskDto);
    TaskDto updateTask(UUID uuid, TaskDto taskDto);
    void deleteTask(UUID uuid);
}
