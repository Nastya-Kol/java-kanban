package model;

import org.junit.jupiter.api.Test;

import static model.TaskStatus.NEW;
import static org.junit.Assert.*;

public class TaskTest {

    @Test
    void equalityOfTaskById() {
        Task task1 = new Task(1,"Test addNewTask1", "Test addNewTask description1", NEW);
        Task task2 = new Task(1,"Test addNewTask1", "Test addNewTask description1", NEW);
        Task task3 = new Task(2,"Test addNewTask2", "Test addNewTask2 description3", NEW);

        int taskID1 = task1.getId();
        int taskID2 = task2.getId();
        int taskID3 = task3.getId();

        assertEquals("Задачи с одинаковым id должны быть равны", taskID1, taskID2);
        assertNotEquals("Задачи с разными id не должны быть равны", taskID1, taskID3);
    }
}