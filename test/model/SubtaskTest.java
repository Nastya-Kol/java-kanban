package model;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.Test;

import static model.TaskStatus.NEW;
import static org.junit.Assert.*;

public class SubtaskTest {
    TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void equalityOfSubtaskById() {
        Subtask subtask1 = new Subtask ("Name Subtask1", "Description Subtask1", NEW, 1);
        Subtask subtask2 = new Subtask ("Name Subtask1", "Description Subtask1", NEW, 1);
        Subtask subtask3 = new Subtask ("Name Subtask3", "Description Subtask3", NEW, 1);

        subtask1.setId(1);
        subtask2.setId(1);
        subtask3.setId(2);

        assertEquals("Подзадачи с одинаковым id должны быть равны", subtask1, subtask2);
        assertNotEquals("Подзадачи с разными id должны быть не равны", subtask1, subtask3);
    }
}