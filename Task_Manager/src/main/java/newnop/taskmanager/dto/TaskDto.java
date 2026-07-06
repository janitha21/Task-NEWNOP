package newnop.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import newnop.taskmanager.entity.TaskStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private UUID uuid;
    
    @NotBlank(message = "Title is mandatory")
    private String title;
    
    private String description;
    
    @NotNull(message = "Status is mandatory")
    private TaskStatus status;
    
    @NotNull(message = "Due date is mandatory")
    private LocalDateTime dueDate;
    
    private UUID ownerUuid;
}
