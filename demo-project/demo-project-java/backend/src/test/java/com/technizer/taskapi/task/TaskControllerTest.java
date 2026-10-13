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

        Task result = taskController.get(1L, 10L);

        assertThat(result).isSameAs(task);
        verify(taskService).getForOwner(1L, 10L);
    }

    @Test
    void get_propagatesNotFoundFromService() {
        when(taskService.getForOwner(1L, 99L)).thenThrow(new NoSuchElementException("Task not found: 99"));

        assertThatThrownBy(() -> taskController.get(1L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Task not found: 99");
    }
}
