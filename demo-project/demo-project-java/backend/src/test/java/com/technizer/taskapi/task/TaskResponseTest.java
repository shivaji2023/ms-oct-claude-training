package com.technizer.taskapi.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TaskResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void from_mapsAllTaskFields() {
        Task task = new Task("title", "description", 7L);
        task.setId(42L);
        task.setCompleted(true);
        task.setPriority(TaskPriority.HIGH);
        Instant createdAt = Instant.parse("2026-10-14T09:00:00Z");
        task.setCreatedAt(createdAt);

        TaskResponse response = TaskResponse.from(task);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.title()).isEqualTo("title");
        assertThat(response.description()).isEqualTo("description");
        assertThat(response.completed()).isTrue();
        assertThat(response.ownerId()).isEqualTo(7L);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void from_defaultsPriorityToMediumForNewTask() {
        Task task = new Task("title", null, 1L);

        TaskResponse response = TaskResponse.from(task);

        assertThat(response.priority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(response.description()).isNull();
    }

    @Test
    void json_containsExistingFieldsPlusPriority() throws Exception {
        Task task = new Task("title", "description", 7L);
        task.setId(42L);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(TaskResponse.from(task)));

        Set<String> keys = new HashSet<>();
        json.fieldNames().forEachRemaining(keys::add);
        assertThat(keys).containsExactlyInAnyOrder(
                "id", "title", "description", "completed", "ownerId", "createdAt", "priority");
        assertThat(json.get("id").isNumber()).isTrue();
        assertThat(json.get("completed").isBoolean()).isTrue();
        assertThat(json.get("ownerId").isNumber()).isTrue();
        assertThat(json.get("priority").asText()).isEqualTo("MEDIUM");
    }

    @Test
    void json_matchesShapeOfTaskEntityPlusPriority() throws Exception {
        Task task = new Task("title", "description", 7L);
        task.setId(42L);

        JsonNode entityJson = objectMapper.readTree(objectMapper.writeValueAsString(task));
        JsonNode responseJson = objectMapper.readTree(objectMapper.writeValueAsString(TaskResponse.from(task)));

        entityJson.fieldNames().forEachRemaining(name -> {
            if (!name.equals("priority")) {
                assertThat(responseJson.get(name)).as("field %s", name).isEqualTo(entityJson.get(name));
            }
        });
    }
}
