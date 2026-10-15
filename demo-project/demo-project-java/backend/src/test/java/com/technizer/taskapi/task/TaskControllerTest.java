package com.technizer.taskapi.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.technizer.taskapi.exception.GlobalExceptionHandler;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
        when(taskService.create(1L, "title", "description", null)).thenReturn(created);

        TaskResponse result = taskController.create(1L,
                new TaskController.CreateTaskRequest("title", "description", null));

        assertThat(result).isEqualTo(TaskResponse.from(created));
        assertThat(result.priority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    void update_returnsResponseForUpdatedTask() {
        Task updated = new Task("new", "desc", 1L);
        when(taskService.update(1L, 10L, "new", "desc", null)).thenReturn(updated);

        TaskResponse result = taskController.update(1L, 10L,
                new TaskController.UpdateTaskRequest("new", "desc", null));

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

    @Test
    void create_passesPriorityFromRequestToService() {
        Task created = new Task("title", "description", 1L);
        created.setPriority(TaskPriority.HIGH);
        when(taskService.create(1L, "title", "description", TaskPriority.HIGH)).thenReturn(created);

        TaskResponse result = taskController.create(1L,
                new TaskController.CreateTaskRequest("title", "description", TaskPriority.HIGH));

        assertThat(result.priority()).isEqualTo(TaskPriority.HIGH);
        verify(taskService).create(1L, "title", "description", TaskPriority.HIGH);
    }

    @Test
    void update_passesPriorityFromRequestToService() {
        Task updated = new Task("new", "desc", 1L);
        updated.setPriority(TaskPriority.LOW);
        when(taskService.update(1L, 10L, "new", "desc", TaskPriority.LOW)).thenReturn(updated);

        TaskResponse result = taskController.update(1L, 10L,
                new TaskController.UpdateTaskRequest("new", "desc", TaskPriority.LOW));

        assertThat(result.priority()).isEqualTo(TaskPriority.LOW);
        verify(taskService).update(1L, 10L, "new", "desc", TaskPriority.LOW);
    }

    @Test
    void emptyStringPriorityInBodyIsRejectedWith400() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"priority\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/tasks/10").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"priority\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).create(any(), any(), any(), any());
        verify(taskService, never()).update(any(), any(), any(), any(), any());
    }
}
