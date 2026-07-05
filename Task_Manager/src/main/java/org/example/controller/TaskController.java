package org.example.controller;

import org.example.dto.TaskDto;
import org.example.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    //----create task
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto taskDto) {
        TaskDto createdTask = taskService.createTask(taskDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    //-----get all tasks
    @GetMapping
    public List<TaskDto> getAllTasks() {
        return taskService.getAllTasks();
    }

    //-----task by id
    @GetMapping("/{uuid}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable UUID uuid) {
        TaskDto taskDto = taskService.getTaskById(uuid);
        return ResponseEntity.ok(taskDto);
    }

    //------update task
    @PutMapping("/{uuid}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable UUID uuid, @RequestBody TaskDto taskDto) {
        TaskDto updatedTask = taskService.updateTask(uuid, taskDto);
        return ResponseEntity.ok(updatedTask);
    }

    //------delete task
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID uuid) {
        taskService.deleteTask(uuid);
        return ResponseEntity.noContent().build();
    }
}
