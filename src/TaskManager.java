import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Subtask> subTasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();

    private int generateCodeID = 0;

    private int generateCodeID() {
        return ++generateCodeID;
    }

    //создание задачи
    public Task saveTasks(Task task) {
        if (task.getId() > 0) {
            return task;
        }

        int newID = generateCodeID();
        task.setId(newID);
        tasks.put(task.getId(), task);
        return task;
    }

    //обновление задачи
    public Task updateTasks(Task task) {
        if (task.getId() == 0) {
            return task;
        }
        if (!tasks.containsKey(task.getId())) {
            return task;
        }
        tasks.put(task.getId(), task);
        return task;
    }

    //создание эпика
    public Epic saveEpics(Epic epic) {
        if (epic.getId() > 0) {
            return epic;
        }

        int newID = generateCodeID();
        epic.setId(newID);
        epics.put(epic.getId(), epic);
        return epic;
    }

    //обновление эпика
    public Epic updateEpics(Epic epic) {
        if (epic.getId() == 0) {
            return epic;
        }
        if (!epics.containsKey(epic.getId())) {
            return epic;
        }

        epics.put(epic.getId(), epic);
        return epic;
    }

    //обновление статуса эпика
    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubTaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
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
    public Subtask saveSubtasks(Subtask subtask) {
        if (subtask.getId() > 0) {
            return subtask;
        }

        if (subtask.getEpic() == 0) {
            return subtask;
        }

        if (!epics.containsKey(subtask.getEpic())) {
            return subtask;
        }
        int newID = generateCodeID();
        subtask.setId(newID);
        subTasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpic());
        epic.getSubTaskIds().add(newID);
        return subtask;
    }

    //обновление подзадачи
    public Subtask updateSubtasks(Subtask subtask) {
        if (subtask.getId() == 0) {
            return subtask;
        }
        if (!subTasks.containsKey(subtask.getId())) {
            return subtask;
        }
        subTasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpic());
        return subtask;
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
    public void deleteAllSubtasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubTaskIds().clear();
        }
    }

    //удаление по id задачи, подзадачи или эпика
    public void removeTasksById(int id) {

        if (tasks.containsKey(id)) {
            tasks.remove(id);
        }
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            for (int subtaskId : epic.getSubTaskIds()) {
                subTasks.remove(subtaskId);
            }
            epics.remove(id);
        }
        if (subTasks.containsKey(id)) {
            Subtask subtask = subTasks.get(id);
            int epicId = subtask.getEpic();
            if (epics.containsKey(epicId)) {
                epics.get(epicId).removeSubtaskId(id);
            }
            subTasks.remove(id);
        }
    }

}
