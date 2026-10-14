package com.technizer.taskapi.task;

import java.time.Instant;

/**
 * API representation of a task. Field names and types match the JSON the {@link Task}
 * entity produced before priorities existed, plus the additive {@code priority}.
 */
public record TaskResponse(
        Long id,
        String title,
        String description,
        boolean completed,
        Long ownerId,
        Instant createdAt,
        TaskPriority priority) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getOwnerId(),
                task.getCreatedAt(),
                task.getPriority());
    }
}
