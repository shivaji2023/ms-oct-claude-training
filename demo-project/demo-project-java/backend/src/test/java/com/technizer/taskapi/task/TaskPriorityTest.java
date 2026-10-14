package com.technizer.taskapi.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskPriorityTest {

    @Test
    void ordinalOrder_rankslowBelowMediumBelowHigh() {
        assertThat(TaskPriority.LOW.ordinal()).isLessThan(TaskPriority.MEDIUM.ordinal());
        assertThat(TaskPriority.MEDIUM.ordinal()).isLessThan(TaskPriority.HIGH.ordinal());
    }

    @Test
    void values_areExactlyLowMediumHigh() {
        assertThat(TaskPriority.values())
                .containsExactly(TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH);
    }

    @Test
    void valueOf_rejectsUnknownAndEmptyValues() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> TaskPriority.valueOf("URGENT"))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> TaskPriority.valueOf(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void newTask_defaultsToMedium() {
        assertThat(new Task("title", "description", 1L).getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    void setPriority_storesTheGivenValue() {
        Task task = new Task("title", "description", 1L);

        task.setPriority(TaskPriority.HIGH);

        assertThat(task.getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void setPriority_nullFallsBackToMedium() {
        Task task = new Task("title", "description", 1L);
        task.setPriority(TaskPriority.HIGH);

        task.setPriority(null);

        assertThat(task.getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }
}
