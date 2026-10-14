package com.technizer.taskapi.task;

import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> list(@AuthenticationPrincipal Long ownerId) {
        return taskService.listForOwner(ownerId).stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse get(@AuthenticationPrincipal Long ownerId, @PathVariable Long id) {
        return TaskResponse.from(taskService.getForOwner(ownerId, id));
    }

    @PostMapping
    public TaskResponse create(@AuthenticationPrincipal Long ownerId, @RequestBody CreateTaskRequest request) {
        return TaskResponse.from(taskService.create(ownerId, request.title(), request.description()));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal Long ownerId, @PathVariable Long id,
                               @RequestBody UpdateTaskRequest request) {
        return TaskResponse.from(taskService.update(ownerId, id, request.title(), request.description()));
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@AuthenticationPrincipal Long ownerId, @PathVariable Long id) {
        return TaskResponse.from(taskService.markCompleted(ownerId, id));
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal Long ownerId, @PathVariable Long id) {
        taskService.delete(ownerId, id);
    }

    public record CreateTaskRequest(@NotBlank String title, String description) {
    }

    public record UpdateTaskRequest(@NotBlank String title, String description) {
    }
}
