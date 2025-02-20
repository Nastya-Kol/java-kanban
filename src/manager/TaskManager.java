package manager;
import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private int generateCodeID = 0;

    private int generateCodeID() {
        return ++generateCodeID;
    }

    //создание задачи
    public int saveTasks(Task task) {
        int newID = generateCodeID();
        task.setId(newID);
        tasks.put(task.getId(), task);
        return newID;
    }

    //обновление задачи
    public void updateTasks(Task task) {
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Такая задача не найдена");
        }
        tasks.put(task.getId(), task);
    }

    //создание эпика
    public int saveEpics(Epic epic) {
        int newID = generateCodeID();
        epic.setId(newID);
        epics.put(epic.getId(), epic);
        return newID;
    }

    //обновление эпика
    public void updateEpics(Epic epic) {
        int epicId = epic.getId();
        if (!epics.containsKey(epicId)) {
            System.out.println("Такой эпик не найден");
        }

        Epic newEpic = epics.get(epicId);
        newEpic.setName(epic.getName());
        newEpic.setDescription(epic.getDescription());
    }

    //обновление статуса эпика
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubTaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        boolean allDone = true;
        boolean allNew = true;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subTasks.get(subtaskId);
            if (subtask == null) return;

            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
        }
        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    //создание подзадачи
    public int saveSubtasks(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpic())) {
            System.out.println("Такой эпик не найден");
        }
        int newID = generateCodeID();
        subtask.setId(newID);
        subTasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpic());
        epic.getSubTaskIds().add(newID);
        updateEpicStatus(newID);
        return newID;
    }

    //обновление подзадачи
    public void updateSubtasks(Subtask subtask) {
        if (!subTasks.containsKey(subtask.getId())) {
            System.out.println("Такая подзадача не найдена");
        }
        subTasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpic());
    }

    //получение всех задач
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    //получение всех подзадач
    public ArrayList<Subtask> getTSubtasks() {
        return new ArrayList<>(subTasks.values());

    }

    //получение всех эпиков
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    //получение задачи по id
    public Task getTaskById(int taskId) {
        return tasks.get(taskId);
    }

    //получение эпика по id
    public Task getEpicById(int epicId) {
        return epics.get(epicId);
    }

    //получение подзадачи по id
    public Task getSubtaskById(int subtaskId) {
        return subTasks.get(subtaskId);
    }

    //удаление всех задач
    public void deleteAllTasks() {
        tasks.clear();
    }

    //удаление всех эпиков
    public void deleteAllEpics() {
        subTasks.clear();
        epics.clear();
    }

    //удаление всех подзадач
    public void deleteAllSubtasks(int epicId) {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubTaskIds().clear();
            updateEpicStatus(epicId);
        }
    }

    public ArrayList<Subtask> getAllSubtaskByEpic (int epicId) {
        ArrayList<Subtask> allSubtasks = new ArrayList <>();
        if (!epics.containsKey(epicId)) {
            return allSubtasks;
        }
        Epic epic = epics.get(epicId);
        for (int subtaskId: epic.getSubTaskIds()) {
           Subtask subtask = subTasks.get(subtaskId);
           if (subtask!=null) {
               allSubtasks.add(subtask);
           }
        }
        return allSubtasks;
    }
}
