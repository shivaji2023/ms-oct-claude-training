package com.technizer.taskapi.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskControllerTest {

    private TaskService taskService;
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        taskController = new TaskController(taskService);
    }

    @Test
    void get_returnsTaskFromServiceForOwner() {
        Task task = new Task("title", "description", 1L);
        when(taskService.getForOwner(1L, 10L)).thenReturn(task);

        TaskResponse result = taskController.get(1L, 10L);

        assertThat(result).isEqualTo(TaskResponse.from(task));
        verify(taskService).getForOwner(1L, 10L);
    }

    @Test
    void list_returnsResponsesForOwnersTasksInServiceOrder() {
        Task first = new Task("first", null, 1L);
        Task second = new Task("second", "d", 1L);
        when(taskService.listForOwner(1L)).thenReturn(java.util.List.of(first, second));

        java.util.List<TaskResponse> result = taskController.list(1L);

        assertThat(result).containsExactly(TaskResponse.from(first), TaskResponse.from(second));
        verify(taskService).listForOwner(1L);
    }

    @Test
    void create_returnsResponseForCreatedTask() {
        Task created = new Task("title", "description", 1L);
        when(taskService.create(1L, "title", "description")).thenReturn(created);

        TaskResponse result = taskController.create(1L,
                new TaskController.CreateTaskRequest("title", "description"));

        assertThat(result).isEqualTo(TaskResponse.from(created));
        assertThat(result.priority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    void update_returnsResponseForUpdatedTask() {
        Task updated = new Task("new", "desc", 1L);
        when(taskService.update(1L, 10L, "new", "desc")).thenReturn(updated);

        TaskResponse result = taskController.update(1L, 10L,
                new TaskController.UpdateTaskRequest("new", "desc"));

        assertThat(result).isEqualTo(TaskResponse.from(updated));
    }

    @Test
    void complete_returnsResponseForCompletedTask() {
        Task completed = new Task("title", null, 1L);
        completed.setCompleted(true);
        when(taskService.markCompleted(1L, 10L)).thenReturn(completed);

        TaskResponse result = taskController.complete(1L, 10L);

        assertThat(result.completed()).isTrue();
    }

    @Test
    void delete_delegatesToServiceWithOwnerId() {
        taskController.delete(1L, 10L);

        verify(taskService).delete(1L, 10L);
    }

    @Test
    void get_propagatesNotFoundFromService() {
        when(taskService.getForOwner(1L, 99L)).thenThrow(new NoSuchElementException("Task not found: 99"));

        assertThatThrownBy(() -> taskController.get(1L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Task not found: 99");
    }
}
