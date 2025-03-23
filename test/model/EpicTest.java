package model;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.Test;

import static model.TaskStatus.NEW;
import static org.junit.Assert.*;

public class EpicTest {
    TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void equalityOfEpicById() {
        Epic epic1 = new Epic ("Name Epic1", "Description Epic1", NEW);
        Epic epic2 = new Epic ("Name Epic1", "Description Epic1", NEW);
        Epic epic3 = new Epic ("Name Epic3", "Description Epic3", NEW);

        epic1.setId(1);
        epic2.setId(1);
        epic3.setId(2);

        assertEquals("Эпики с одинаковым id должны быть равны", epic1, epic2);
        assertNotEquals("Эпики с разными id должны быть не равны", epic1, epic3);
    }
}