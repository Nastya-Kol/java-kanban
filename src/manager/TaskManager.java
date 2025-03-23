package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    //создание задачи
    int saveTasks(Task task);

    //обновление задачи
    void updateTasks(Task task);

    //создание эпика
    int saveEpics(Epic epic);

    //обновление эпика
    void updateEpics(Epic epic);

    //создание подзадачи
    int saveSubtasks(Subtask subtask);

    //обновление подзадачи
    void updateSubtasks(Subtask subtask);

    //получение всех задач
    List<Task> getTasks();

    //получение всех подзадач
    List<Subtask> getSubtasks();

    //получение всех эпиков
    List<Epic> getEpics();

    //получение задачи по id
    Task getTaskById(int taskId);

    //получение эпика по id
    Epic getEpicById(int epicId);

    //получение подзадачи по id
    Subtask getSubtaskById(int subtaskId);

    //удаление всех задач
    void deleteAllTasks();

    //удаление всех эпиков
    void deleteAllEpics();

    //удаление всех подзадач
    void deleteAllSubtasks(int epicId);

    //удаление задачи по id
    void removeTasksById(int taskId);

    //удаление эпика по id
    void removeEpicsById(int epicId);

    //удаление подзадачи по id
    void removeSubtasksById(int subtaskId);

    List<Subtask> getAllSubtaskByEpic(int epicId);

    List<Task> getHistory();
}
