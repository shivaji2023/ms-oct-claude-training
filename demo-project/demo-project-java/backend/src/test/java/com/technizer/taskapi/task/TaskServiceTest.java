package com.technizer.taskapi.task;

// TRAINING NOTE: This file is intentionally left with only a placeholder.
// In Module 6 (Skills) and Module 9-10 (Spec-Driven Development / Spec Kit),
// participants will use Claude Code to generate real test coverage for
// TaskService, including the missing ownership check on markCompleted()
// and delete() surfaced during the Module 13 security-review demo.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);
    }

    @Test
    void placeholder() {
        // Replaced live during the session.
    }

    @Test
    void listForOwner_returnsOwnersTasksInCreationOrder() {
        Task first = new Task("first", null, 1L);
        Task second = new Task("second", null, 1L);
        when(taskRepository.findByOwnerIdOrderByIdAsc(1L)).thenReturn(List.of(first, second));

        List<Task> result = taskService.listForOwner(1L);

        assertThat(result).containsExactly(first, second);
    }

    @Test
    void listForOwner_queriesOnlyByTheGivenOwnerId() {
        when(taskRepository.findByOwnerIdOrderByIdAsc(2L)).thenReturn(List.of());

        List<Task> result = taskService.listForOwner(2L);

        assertThat(result).isEmpty();
        verify(taskRepository).findByOwnerIdOrderByIdAsc(2L);
        verifyNoMoreInteractions(taskRepository);
    }
}
