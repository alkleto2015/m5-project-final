package com.javarush.jira.bugtracking.task;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.bugtracking.Handlers;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

class TaskServiceTest extends AbstractControllerTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private Handlers.TaskExtHandler handler;

    @Test
    @Transactional
    void getInWorkAndTestingDuration() {
        // беремо існуючу задачу з бази (ID = 1L)
        Task task = handler.getRepository().findFullById(1L).orElseThrow();

        // очищаємо минулі активності задачі
        task.getActivities().clear();

        // створюємо тестові активності
        Activity a1 = new Activity();
        a1.setTaskId(task.getId()); // Замінили  setTask на setTaskId (це те поле, щто у нас в Activity)
        a1.setStatusCode("in_progress");
        a1.setUpdated(LocalDateTime.of(2026, 8, 20, 10, 0));

        Activity a2 = new Activity();
        a2.setTaskId(task.getId());
        a2.setStatusCode("ready_for_review");
        a2.setUpdated(LocalDateTime.of(2026, 8, 21, 10, 0));

        Activity a3 = new Activity();
        a3.setTaskId(task.getId());
        a3.setStatusCode("done");
        a3.setUpdated(LocalDateTime.of(2026, 8, 21, 18, 0));

        task.getActivities().addAll(List.of(a1, a2, a3));

        // визиваємо методи розрахунка, що будемо перевіряти
        Duration inWork = taskService.getInWorkDuration(task);
        Duration testing = taskService.getTestingDuration(task);

        //виводимо результати у консоль
        System.out.println("=== Time in work: " + inWork.toHours() + " hours ===");
        System.out.println("=== Time in testing: " + testing.toHours() + " hours ===");

        // перевіряємо результати
        Assertions.assertNotNull(inWork);
        Assertions.assertEquals(24, inWork.toHours(), "In-work duration (in_progress -> ready_for_review) must be 24h");

        Assertions.assertNotNull(testing);
        Assertions.assertEquals(8, testing.toHours(), "Testing duration (ready_for_review -> done) must be 8h");
    }
}
