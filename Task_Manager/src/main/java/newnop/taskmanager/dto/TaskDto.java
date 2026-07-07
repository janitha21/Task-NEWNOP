package newnop.taskmanager.dto;

import lombok.*;

import java.util.UUID;
import java.time.LocalDateTime;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import newnop.taskmanager.entity.TaskStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskDto {
    private UUID uuid;
    
    @NotBlank(message = "Title is mandatory")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Status is mandatory")
    private TaskStatus status;
    
    @NotNull(message = "Due date is mandatory")
    @FutureOrPresent(message = "Due date must be in the present or future")
    private LocalDateTime dueDate;
    
    private UUID ownerUuid;
}
