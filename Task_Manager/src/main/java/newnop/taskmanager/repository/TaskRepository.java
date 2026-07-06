package newnop.taskmanager.repository;

import newnop.taskmanager.entity.Task;
import newnop.taskmanager.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByOwnerUuid(UUID ownerUuid, Pageable pageable);
    Page<Task> findByStatusAndOwnerUuid(TaskStatus status, UUID ownerUuid, Pageable pageable);
}
