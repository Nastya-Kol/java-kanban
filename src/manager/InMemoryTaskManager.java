package manager;

import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Subtask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private final HistoryManager historyManager = Managers.getDefaultHistoryMemory();
    private int generateCodeID = 0;

    private int generateCodeID() {
        return ++generateCodeID;
    }

    //создание задачи
    @Override
    public int saveTasks(Task task) {
        int newID = generateCodeID();
        task.setId(newID);
        tasks.put(task.getId(), task);
        return newID;
    }

    //обновление задачи
    @Override
    public void updateTasks(Task task) {
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Такая задача не найдена");
            return;
        }
        tasks.put(task.getId(), task);
    }

    //создание эпика
    @Override
    public int saveEpics(Epic epic) {
        int newID = generateCodeID();
        epic.setId(newID);
        epics.put(epic.getId(), epic);
        return newID;
    }

    //обновление эпика
    @Override
    public void updateEpics(Epic epic) {
        int epicId = epic.getId();
        if (!epics.containsKey(epicId)) {
            System.out.println("Такой эпик не найден");
            return;
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
    @Override
    public int saveSubtasks(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpic())) {
            System.out.println("Такой эпик не найден");
            return -1;
        }

        int newID = generateCodeID();
        subtask.setId(newID);
        subTasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpic());
        epic.getSubTaskIds().add(newID);
        updateEpicStatus(subtask.getEpic());
        return newID;
    }

    //обновление подзадачи
    @Override
    public void updateSubtasks(Subtask subtask) {
        if (!subTasks.containsKey(subtask.getId())) {
            System.out.println("Такая подзадача не найдена");
            return;
        }
        subTasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpic());
    }

    //получение всех задач
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    //получение всех подзадач
    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subTasks.values());

    }

    //получение всех эпиков
    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    //получение задачи по id
    @Override
    public Task getTaskById(int taskId) {
        Task task = tasks.get(taskId);
        historyManager.addTask(task);
        return task;
    }

    //получение эпика по id
    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = epics.get(epicId);
        historyManager.addTask(epic);
        return epic;
    }

    //получение подзадачи по id
    @Override
    public Subtask getSubtaskById(int subtaskId) {
        Subtask subtask = subTasks.get(subtaskId);
        historyManager.addTask(subtask);
        return subtask;
    }

    //удаление всех задач
    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    //удаление всех эпиков
    @Override
    public void deleteAllEpics() {
        subTasks.clear();
        epics.clear();
    }

    //удаление всех подзадач
    @Override
    public void deleteAllSubtasks(int epicId) {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubTaskIds().clear();
            updateEpicStatus(epicId);
        }
    }

    //удаление задачи по id
    @Override
    public void removeTasksById(int taskId) {
        if (!tasks.containsKey(taskId)) {
            return;
        }
        tasks.remove(taskId);
    }

    //удаление эпика по id
    @Override
    public void removeEpicsById(int epicId) {
        if (!epics.containsKey(epicId)) {
            return;
        }
        Epic epic = epics.get(epicId);
        for (int subtaskId : epic.getSubTaskIds()) {
            subTasks.remove(subtaskId);
        }
        epics.remove(epicId);
    }

    //удаление подзадачи по id
    @Override
    public void removeSubtasksById(int subtaskId) {
        if (!subTasks.containsKey(subtaskId)) {
            return;
        }
        Subtask subtask = subTasks.get(subtaskId);
        int epicId = subtask.getEpic();
        if (epics.containsKey(epicId)) {
            epics.get(epicId).removeSubtaskId(subtaskId);
            updateEpicStatus(epicId);
        }
        subTasks.remove(subtaskId);
    }


    @Override
    public ArrayList<Subtask> getAllSubtaskByEpic(int epicId) {
        ArrayList<Subtask> allSubtasks = new ArrayList<>();
        if (!epics.containsKey(epicId)) {
            return allSubtasks;
        }
        Epic epic = epics.get(epicId);
        for (int subtaskId : epic.getSubTaskIds()) {
            Subtask subtask = subTasks.get(subtaskId);
            if (subtask != null) {
                allSubtasks.add(subtask);
            }
        }
        return allSubtasks;
    }
    @Override
    public List<Task> getHistory(){
        return historyManager.getHistory();
    }
}
