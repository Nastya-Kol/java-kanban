package manager;

import exception.ManagerSaveException;
import exception.TimeConflictException;
import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Subtask> subTasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    private final HistoryManager historyManager = Managers.getDefaultHistoryMemory();
    protected int generateCodeID = 0;

    protected int generateCodeID() {
        return ++generateCodeID;
    }

    //создание задачи
    @Override
    public int saveTasks(Task task) {
        if (hasTimeOverlap(task)) {
            throw new ManagerSaveException("Задача пересекается по времени с существующей задачей");
        }
        int newID = generateCodeID();
        task.setId(newID);
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        return newID;
    }

    //обновление задачи
    @Override
    public void updateTasks(Task task) {
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Такая задача не найдена");
            return;
        }
        Task existingTask = tasks.get(task.getId());
        if (hasTimeOverlap(task)) {
            throw new TimeConflictException("Задача пересекается по времени с существующей задачей");
        }

        if (existingTask != null) {
            removeFromPrioritized(existingTask);
        }
        tasks.put(task.getId(), task);
        addToPrioritized(task);
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
        if (hasTimeOverlap(subtask)) {
            throw new ManagerSaveException("Подзадача пересекается по времени с существующей задачей");
        }
        int newID = generateCodeID();
        subtask.setId(newID);
        subTasks.put(subtask.getId(), subtask);
        addToPrioritized(subtask);

        Epic epic = epics.get(subtask.getEpic());
        epic.getSubTaskIds().add(newID);
        updateEpicStatus(subtask.getEpic());
        updateEpicTimes(subtask.getEpic());
        return newID;
    }

    //обновление подзадачи
    @Override
    public void updateSubtasks(Subtask subtask) {
        if (!subTasks.containsKey(subtask.getId())) {
            System.out.println("Такая подзадача не найдена");
            return;
        }
        Subtask existingSubtask = subTasks.get(subtask.getId());
        if (hasTimeOverlap(subtask)) {
            throw new TimeConflictException("Задача пересекается по времени с существующей задачей");
        }

        if (existingSubtask != null) {
            removeFromPrioritized(existingSubtask);
        }

        subTasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpic());
        addToPrioritized(subtask);
        updateEpicTimes(subtask.getEpic());
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
        for (Task task : tasks.values()) {
            removeFromPrioritized(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    //удаление всех эпиков
    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            for (int subtaskId : epic.getSubTaskIds()) {
                Subtask subtask = subTasks.get(subtaskId);
                if (subtask != null) {
                    removeFromPrioritized(subtask);
                }
                historyManager.remove(subtaskId);
            }
            historyManager.remove(epic.getId());
        }
        subTasks.clear();
        epics.clear();
    }

    //удаление всех подзадач
    @Override
    public void deleteAllSubtasks(int epicId) {

        Epic epic = epics.get(epicId);
        for (int subtaskId : epic.getSubTaskIds()) {
            Subtask subtask = subTasks.get(subtaskId);
            if (subtask != null) {
                removeFromPrioritized(subtask);
            }
            historyManager.remove(subtaskId);
        }
        subTasks.clear();
        for (Epic epic1 : epics.values()) {
            epic1.getSubTaskIds().clear();
            updateEpicStatus(epicId);
            updateEpicTimes(epicId);
        }
    }

    //удаление задачи по id
    @Override
    public void removeTasksById(int taskId) {
        if (!tasks.containsKey(taskId)) {
            return;
        }
        Task task = tasks.remove(taskId);
        historyManager.remove(taskId);
        removeFromPrioritized(task);
    }

    //удаление эпика по id
    @Override
    public void removeEpicsById(int epicId) {
        if (!epics.containsKey(epicId)) {
            return;
        }
        Epic epic = epics.get(epicId);
        for (int subtaskId : epic.getSubTaskIds()) {
            Subtask subtask = subTasks.remove(subtaskId);
            if (subtask != null) {
                removeFromPrioritized(subtask);
                subTasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
        }
        epics.remove(epicId);
        historyManager.remove(epicId);
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
            updateEpicTimes(epicId);
        }
        subTasks.remove(subtaskId);
        removeFromPrioritized(subtask);
        historyManager.remove(subtaskId);
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
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicTimes(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> subtasks = epic.getSubTaskIds().stream()
                .map(subTasks::get)
                .filter(subtask -> subtask.getStartTime() != null && subtask.getDuration() != null)
                .collect(Collectors.toList());

        if (subtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
            return;
        }
        // Находим самое раннее время начала
        LocalDateTime earliestStart = subtasks.stream()
                .map(Subtask::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Находим самое позднее время окончания
        LocalDateTime latestEnd = subtasks.stream()
                .map(subtask -> subtask.getStartTime().plus(subtask.getDuration())) // Исправлено: plus() вместо plusMinutes()
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Суммируем длительности всех подзадач
        Duration totalDuration = Duration.ZERO;
        for (Subtask subtask : subtasks) {
            totalDuration = totalDuration.plus(subtask.getDuration());
        }

        epic.setStartTime(earliestStart);
        epic.setEndTime(latestEnd);
        epic.setDuration(totalDuration);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void addToPrioritized(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    private boolean isOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getEndTime() == null ||
                task2.getStartTime() == null || task2.getEndTime() == null) {
            return false;
        }

        return task1.getStartTime().isBefore(task2.getEndTime()) &&
                task1.getEndTime().isAfter(task2.getStartTime());
    }

    private boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(task -> !task.equals(newTask))
                .anyMatch(existingTask -> isOverlap(newTask, existingTask));
    }
}
