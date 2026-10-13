package com.technizer.taskapi.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskServiceGetTest {

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);
    }

    @Test
    void getForOwner_returnsTaskForOwner() {
        Task existing = new Task("title", "description", 1L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));

        Task result = taskService.getForOwner(1L, 10L);

        assertThat(result).isSameAs(existing);
    }

    @Test
    void getForOwner_throwsNotFoundWhenTaskMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getForOwner(1L, 99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getForOwner_throwsNotFoundWhenOwnerIdIsNull() {
        Task existing = new Task("title", "desc", 1L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> taskService.getForOwner(null, 10L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getForOwner_throwsNotFoundWithTaskIdInMessage() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getForOwner(1L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Task not found: 99");
    }

    @Test
    void getForOwner_throwsNotFoundWhenTaskBelongsToAnotherUser() {
        Task others = new Task("title", "desc", 2L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(others));

        assertThatThrownBy(() -> taskService.getForOwner(1L, 10L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
