package com.technizer.taskapi.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskServiceUpdateTest {

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);
    }

    @Test
    void update_changesTitleAndDescriptionForOwner() {
        Task existing = new Task("old title", "old description", 1L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.update(1L, 10L, "new title", "new description", null);

        assertThat(result.getTitle()).isEqualTo("new title");
        assertThat(result.getDescription()).isEqualTo("new description");
        assertThat(result.getOwnerId()).isEqualTo(1L);
        verify(taskRepository).save(existing);
    }

    @Test
    void update_allowsNullDescription() {
        Task existing = new Task("title", "description", 1L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.update(1L, 10L, "title", null, null);

        assertThat(result.getDescription()).isNull();
    }

    @Test
    void update_throwsNotFoundWhenTaskMissing() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(1L, 99L, "title", "desc", null))
                .isInstanceOf(NoSuchElementException.class);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_throwsNotFoundWhenTaskBelongsToAnotherUser() {
        Task others = new Task("title", "desc", 2L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(others));

        assertThatThrownBy(() -> taskService.update(1L, 10L, "hijacked", "desc", null))
                .isInstanceOf(NoSuchElementException.class);
        assertThat(others.getTitle()).isEqualTo("title");
        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_rejectsBlankTitle() {
        assertThatThrownBy(() -> taskService.update(1L, 10L, "   ", "desc", null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_rejectsNullTitle() {
        assertThatThrownBy(() -> taskService.update(1L, 10L, null, "desc", null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_appliesNewPriority() {
        Task existing = new Task("title", "desc", 1L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.update(1L, 10L, "title", "desc", TaskPriority.LOW);

        assertThat(result.getPriority()).isEqualTo(TaskPriority.LOW);
    }

    @Test
    void update_leavesPriorityUnchangedWhenNull() {
        Task existing = new Task("title", "desc", 1L);
        existing.setPriority(TaskPriority.HIGH);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.update(1L, 10L, "new", "desc", null);

        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void update_priorityChangeByNonOwnerIsNotFoundAndLeavesPriority() {
        Task others = new Task("title", "desc", 2L);
        others.setPriority(TaskPriority.LOW);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(others));

        assertThatThrownBy(() -> taskService.update(1L, 10L, "title", "desc", TaskPriority.HIGH))
                .isInstanceOf(NoSuchElementException.class);
        assertThat(others.getPriority()).isEqualTo(TaskPriority.LOW);
        verify(taskRepository, never()).save(any());
    }
}
