package test;

import manager.InMemoryTaskManager;
import model.Task;
import model.Epic;
import model.Subtask;
import manager.TaskManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static model.TaskStatus.NEW;
import static org.junit.Assert.*;

import java.util.List;

public class InMemoryTaskManagerTest {
    TaskManager taskManager = new InMemoryTaskManager();

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", NEW);
        Task task2 = new Task("Test addNewTask2", "Test addNewTask2 description2", NEW);
        final int taskId = taskManager.saveTasks(task);
        final int taskId2 = taskManager.saveTasks(task2);

        final Task savedTask = taskManager.getTaskById(taskId);
        final Task savedTask2 = taskManager.getTaskById(taskId2);

        assertNotNull("Задача не найдена.", savedTask);
        assertEquals("Задачи не совпадают.", task, savedTask);

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull("Задачи не возвращаются.", tasks);
        assertEquals("Неверное количество задач.", 2, tasks.size());
        assertEquals("Задачи не совпадают.", task, tasks.get(0));
    }

    Epic epic = new Epic("Test addNewEpic", "Test addNewEpic description", NEW);
    Epic epic2 = new Epic("Test addNewEpic2", "Test addNewEpic2 description2", NEW);
    final int epicId = taskManager.saveEpics(epic);
    final int epicId2 = taskManager.saveEpics(epic2);

    @Test
    void addNewEpic() {

        final Epic savedEpic = taskManager.getEpicById(epicId);
        final Epic savedEpic2 = taskManager.getEpicById(epicId2);

        assertNotNull("Эпик не найден.", savedEpic);
        assertEquals("Эпики  не совпадают.", epic, savedEpic);

        final List<Epic> epics = taskManager.getEpics();

        assertNotNull("Эпики не возвращаются.", epics);
        assertEquals("Неверное количество Эпиков.", 2, epics.size());
        assertEquals("Эпики не совпадают.", epic, epics.get(0));
    }

    @Test
    void addNewSubtask() {
        Subtask subtask = new Subtask("Test addNewSubtask", "Test addNewSubtask description", NEW, epic.getId());
        Subtask subtask2 = new Subtask("Test addNewSubtask2", "Test addNewSubtask2 description2", NEW, epic.getId());
        final int subtaskId = taskManager.saveSubtasks(subtask);
        final int subtaskId2 = taskManager.saveSubtasks(subtask2);

        final Subtask savedSubtask = taskManager.getSubtaskById(subtaskId);
        final Subtask savedSubtask2 = taskManager.getSubtaskById(subtaskId2);

        assertNotNull("Подзадача не найдена.", savedSubtask);
        assertEquals("Подзадачи не совпадают.", subtask, savedSubtask);

        final List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull("Подзадачи не возвращаются.", subtasks);
        assertEquals("Неверное количество Подзадач.", 2, subtasks.size());
        assertEquals("Подзадачи не совпадают.", subtask, subtasks.get(0));
    }

    @Test
    void findNotExistentTask(){
        Task task = taskManager.getTaskById(34);

        Assertions.assertNull(task, "Задача не существует, должна быть null");
    }

}