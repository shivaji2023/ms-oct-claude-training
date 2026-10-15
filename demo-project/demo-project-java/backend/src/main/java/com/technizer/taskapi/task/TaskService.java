package com.technizer.taskapi.task;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> listForOwner(Long ownerId) {
        return taskRepository.findByOwnerIdOrderByIdAsc(ownerId);
    }

    public Task getForOwner(Long ownerId, Long taskId) {
        // A task owned by someone else is reported as not found so its existence isn't leaked.
        return taskRepository.findById(taskId)
                .filter(t -> t.getOwnerId().equals(ownerId))
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + taskId));
    }

    public Task create(Long ownerId, String title, String description, TaskPriority priority) {
        Task task = new Task(title, description, ownerId);
        task.setPriority(priority != null ? priority : TaskPriority.MEDIUM);
        return taskRepository.save(task);
    }

    public Task update(Long ownerId, Long taskId, String title, String description, TaskPriority priority) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }

        // A task owned by someone else is reported as not found so its existence isn't leaked.
        Task task = taskRepository.findById(taskId)
                .filter(t -> t.getOwnerId().equals(ownerId))
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + taskId));

        task.setTitle(title);
        task.setDescription(description);
        if (priority != null) {
            task.setPriority(priority);
        }
        return taskRepository.save(task);
    }

    public Task markCompleted(Long ownerId, Long taskId) {
        // A task owned by someone else is reported as not found so its existence isn't leaked.
        Task task = getForOwner(ownerId, taskId);

        task.setCompleted(true);
        return taskRepository.save(task);
    }

    public void delete(Long ownerId, Long taskId) {
        // A task owned by someone else is reported as not found so its existence isn't leaked.
        Task task = getForOwner(ownerId, taskId);

        taskRepository.delete(task);
    }
}
