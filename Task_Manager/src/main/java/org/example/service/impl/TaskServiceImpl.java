package org.example.service.impl;

import org.example.dto.TaskDto;
import org.example.entity.Task;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.TaskRepository;
import org.example.service.TaskService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, ModelMapper modelMapper) {
        this.taskRepository = taskRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(task -> modelMapper.map(task, TaskDto.class))
                .toList();
    }

    @Override
    public TaskDto getTaskById(UUID uuid) {
        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with UUID: " + uuid));
        return modelMapper.map(task, TaskDto.class);
    }

    @Override
    public TaskDto createTask(TaskDto taskDto) {
        Task task = modelMapper.map(taskDto, Task.class);
        Task savedTask = taskRepository.save(task);
        return modelMapper.map(savedTask, TaskDto.class);
    }

    @Override
    public TaskDto updateTask(UUID uuid, TaskDto taskDto) {
        Task existingTask = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with UUID: " + uuid));
        
        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setCompleted(taskDto.isCompleted());
        
        Task updatedTask = taskRepository.save(existingTask);
        return modelMapper.map(updatedTask, TaskDto.class);
    }

    @Override
    public void deleteTask(UUID uuid) {
        Task task = taskRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with UUID: " + uuid));
        taskRepository.delete(task);
    }
}
