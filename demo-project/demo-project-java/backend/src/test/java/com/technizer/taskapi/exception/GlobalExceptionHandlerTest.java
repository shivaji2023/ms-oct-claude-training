package com.technizer.taskapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUnreadableBody_returnsBadRequest() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("JSON parse error: Cannot deserialize value of type TaskPriority",
                        mock(HttpInputMessage.class));

        ResponseEntity<Map<String, String>> response = handler.handleUnreadableBody(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Invalid request body");
    }

    @Test
    void handleUnreadableBody_doesNotLeakInternalDetails() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("com.technizer.taskapi.task.TaskPriority secret detail",
                        mock(HttpInputMessage.class));

        ResponseEntity<Map<String, String>> response = handler.handleUnreadableBody(ex);

        assertThat(response.getBody().get("error")).doesNotContain("technizer").doesNotContain("secret");
    }

    @Test
    void handleTypeMismatch_returnsBadRequestNamingTheParameter() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "URGENT", Enum.class, "priority", mock(MethodParameter.class), new IllegalArgumentException("bad"));

        ResponseEntity<Map<String, String>> response = handler.handleTypeMismatch(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Invalid value for parameter: priority");
    }

    @Test
    void handleTypeMismatch_doesNotEchoTheSubmittedValue() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "<script>", Boolean.class, "completed", mock(MethodParameter.class), new IllegalArgumentException("bad"));

        ResponseEntity<Map<String, String>> response = handler.handleTypeMismatch(ex);

        assertThat(response.getBody().get("error")).doesNotContain("<script>");
    }

    @Test
    void existingHandlers_stillMapNotFoundAndBadRequest() {
        assertThat(handler.handleNotFound(new NoSuchElementException("Task not found: 1")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleBadRequest(new IllegalArgumentException("bad")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
