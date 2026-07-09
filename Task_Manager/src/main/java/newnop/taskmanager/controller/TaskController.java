package newnop.taskmanager.controller;

import newnop.taskmanager.dto.TaskDto;
import newnop.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import newnop.taskmanager.entity.TaskStatus;
import java.util.UUID;

import newnop.taskmanager.constant.AppConstants;

@RestController
@RequestMapping(AppConstants.TASKS_API)
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    //----create task
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto, Authentication authentication) {
        System.out.println("task" +taskDto);
        UUID userUuid = UUID.fromString(authentication.getName());
        TaskDto createdTask = taskService.createTask(taskDto, userUuid);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    //-----get all tasks (ADMIN only — sees every task in the system)
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<TaskDto>> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) UUID owner,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(taskService.getAllTasks(status, owner, pageable));
    }

    //-----get current user's own tasks (any authenticated user)
    @GetMapping("/my")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Page<TaskDto>> getMyTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        UUID userUuid = UUID.fromString(authentication.getName());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(taskService.getAllTasks(status, userUuid, pageable));
    }


    //-----task by id
    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable UUID uuid, Authentication authentication) {
        TaskDto taskDto = taskService.getTaskById(uuid);
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !taskDto.getOwnerUuid().toString().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(taskDto);
    }

    //------update task
    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<TaskDto> updateTask(@PathVariable UUID uuid, @Valid @RequestBody TaskDto taskDto, Authentication authentication) {
        UUID userUuid = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        TaskDto updatedTask = taskService.updateTask(uuid, taskDto, userUuid, isAdmin);
        return ResponseEntity.ok(updatedTask);
    }

    //------delete task
    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID uuid, Authentication authentication) {
        UUID userUuid = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
        taskService.deleteTask(uuid, userUuid, isAdmin);
        return ResponseEntity.noContent().build();
    }
}
