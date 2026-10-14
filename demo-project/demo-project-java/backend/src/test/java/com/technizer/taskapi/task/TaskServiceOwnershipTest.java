package com.technizer.taskapi.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskServiceOwnershipTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_ID = 2L;
    private static final Long TASK_ID = 10L;

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);
    }

    private Task taskOwnedBy(Long ownerId) {
        Task task = new Task("title", "description", ownerId);
        task.setId(TASK_ID);
        return task;
    }

    @Test
    void markCompleted_completesTaskForOwner() {
        Task task = taskOwnedBy(OWNER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.markCompleted(OWNER_ID, TASK_ID);

        assertThat(result.isCompleted()).isTrue();
        verify(taskRepository).save(task);
    }

    @Test
    void markCompleted_reportsNotFoundForAnotherUsersTask() {
        Task task = taskOwnedBy(OWNER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.markCompleted(OTHER_ID, TASK_ID))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessageContaining("Task not found");

        assertThat(task.isCompleted()).isFalse();
        verify(taskRepository, never()).save(any());
    }

    @Test
    void markCompleted_reportsNotFoundWhenTaskMissing() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.markCompleted(OWNER_ID, TASK_ID))
                .isInstanceOf(java.util.NoSuchElementException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void delete_deletesTaskForOwner() {
        Task task = taskOwnedBy(OWNER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        taskService.delete(OWNER_ID, TASK_ID);

        verify(taskRepository).delete(task);
    }

    @Test
    void delete_reportsNotFoundForAnotherUsersTaskAndDeletesNothing() {
        Task task = taskOwnedBy(OWNER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.delete(OTHER_ID, TASK_ID))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessageContaining("Task not found");

        verify(taskRepository, never()).delete(any());
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void delete_reportsNotFoundWhenTaskMissing() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(OWNER_ID, TASK_ID))
                .isInstanceOf(java.util.NoSuchElementException.class);

        verify(taskRepository, never()).delete(any());
    }

    @Test
    void ownershipFailuresDoNotRevealWhetherTheTaskExists() {
        Task task = taskOwnedBy(OWNER_ID);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        Throwable foreign = org.assertj.core.api.Assertions.catchThrowable(
                () -> taskService.delete(OTHER_ID, TASK_ID));
        Throwable missing = org.assertj.core.api.Assertions.catchThrowable(
                () -> taskService.delete(OTHER_ID, 99L));

        assertThat(foreign).isInstanceOf(java.util.NoSuchElementException.class);
        assertThat(missing).isInstanceOf(java.util.NoSuchElementException.class);
        assertThat(foreign.getMessage().replace("10", "N")).isEqualTo(missing.getMessage().replace("99", "N"));
    }
}
