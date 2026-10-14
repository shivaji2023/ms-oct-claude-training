package com.technizer.taskapi.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByOwnerId(Long ownerId);

    /** Owner's tasks in creation order (oldest first); the baseline order for the task list. */
    List<Task> findByOwnerIdOrderByIdAsc(Long ownerId);
}
