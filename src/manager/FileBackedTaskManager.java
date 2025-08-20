package manager;

import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;
import model.TaskType;

import java.io.File;
import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String HEADER = "id,type,name,status,description,epic,duration,endTime";
    int generateCodeID = 0;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Новый метод сохранения состояния
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Записываем заголовок
            writer.write(HEADER);
            writer.newLine();

            // Создаем потоки строк для каждой коллекции
            Stream<String> taskStream = tasks.values().stream()
                    .map(this::toString);
            Stream<String> epicStream = epics.values().stream()
                    .map(this::toString);
            Stream<String> subtaskStream = subTasks.values().stream()
                    .map(this::toString);

            // Объединяем все потоки и записываем в файл
            Stream.concat(Stream.concat(taskStream, epicStream), subtaskStream)
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new ManagerSaveException("Ошибка записи задачи в файл", e);
                        }
                    });
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    // Преобразование задачи в строку
    private String toString(Task task) {
        TaskType type;
        if (task instanceof Epic) {
            type = TaskType.EPIC;
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK;
        } else {
            type = TaskType.TASK;
        }

        String epicId = "";
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpic());
        }

        String startTime = "";
        if (task.getStartTime() != null) {
            startTime = task.getStartTime().toString();
        }

        String duration = "";
        if (task.getDuration() != null) {
            duration = String.valueOf(task.getDuration().toMinutes());
        }

        String endTime = "";
        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            if (epic.getEndTime() != null) {
                endTime = epic.getEndTime().toString();
            }
        } else {
            if (task.getEndTime() != null) {
                endTime = task.getEndTime().toString();
            }
        }

        return String.join(",",
                String.valueOf(task.getId()),
                type.name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                epicId,
                startTime,
                duration,
                endTime
        );
    }

    // Метод загрузки из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            // Пропускаем заголовок
            for (int i = 1; i < lines.length; i++) {
                Task task = fromString(lines[i]);
                if (task != null) {
                    if (task.getId() > maxId) {
                        maxId = task.getId();
                    }
                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        Subtask subtask = (Subtask) task;
                        manager.subTasks.put(subtask.getId(), subtask);

                        Epic epic = manager.epics.get(subtask.getEpic());
                        if (epic != null) {
                            epic.getSubTaskIds().add(subtask.getId());
                        }

                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                }
            }
            manager.setLastId(maxId);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
        return manager;
    }

    // Метод создания задачи из строки
    private static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String[] parts = value.split(",", -1); // -1 сохраняет пустые значения
        if (parts.length < 9) {
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            TaskType type = TaskType.valueOf(parts[1].trim());
            String name = parts[2].trim();
            TaskStatus status = TaskStatus.valueOf(parts[3].trim());
            String description = parts[4].trim();
            String epicIdStr = parts[5].trim();
            String startTimeStr = parts[6].trim();
            String durationStr = parts[7].trim();
            String endTimeStr = parts[8].trim();

            LocalDateTime startTime = null;
            if (!startTimeStr.isEmpty()) {
                startTime = LocalDateTime.parse(startTimeStr);
            }

            Duration duration = null;
            if (!durationStr.isEmpty()) {
                duration = Duration.ofMinutes(Long.parseLong(durationStr));
            }

            LocalDateTime endTime = null;
            if (!endTimeStr.isEmpty()) {
                endTime = LocalDateTime.parse(endTimeStr);
            }

            switch (type) {
                case TASK:
                    Task task = new Task(id, name, description, status);
                    task.setStartTime(startTime);
                    task.setDuration(duration);
                    return task;

                case EPIC:
                    Epic epic = new Epic(id, name, description, status);
                    epic.setStartTime(startTime);
                    epic.setEndTime(endTime);
                    return epic;

                case SUBTASK:
                    int epicId = Integer.parseInt(epicIdStr);
                    Subtask subtask = new Subtask(id, name, description, status, epicId);
                    subtask.setStartTime(startTime);
                    subtask.setDuration(duration);
                    return subtask;
                default:

                    throw new IllegalStateException("Unexpected value: " + type);
            }
        } catch (NumberFormatException e) {
            throw new ManagerSaveException("Ошибка формата числа в строке: " + value, e);
        } catch (IllegalArgumentException e) {
            throw new ManagerSaveException("Ошибка формата данных в строке: " + value, e);
        }
    }

    public void setLastId(int lastId) {
        this.generateCodeID = lastId;
    }

    @Override
    public int saveTasks(Task task) {
        int result = super.saveTasks(task);
        save();
        return result;
    }

    @Override
    public void updateTasks(Task task) {
        super.updateTasks(task);
        save();
    }

    @Override
    public int saveEpics(Epic epic) {
        int result = super.saveEpics(epic);
        save();
        return result;
    }

    @Override
    public void updateEpics(Epic epic) {
        super.updateEpics(epic);
        save();
    }

    @Override
    public int saveSubtasks(Subtask subtask) {
        int result = super.saveSubtasks(subtask);
        save();
        return result;
    }

    @Override
    public void updateSubtasks(Subtask subtask) {
        super.updateSubtasks(subtask);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks(int epicId) {
        super.deleteAllSubtasks(epicId);
        save();
    }

    @Override
    public void removeTasksById(int taskId) {
        super.removeTasksById(taskId);
        save();
    }

    @Override
    public void removeEpicsById(int epicId) {
        super.removeEpicsById(epicId);
        save();
    }

    @Override
    public void removeSubtasksById(int subtaskId) {
        super.removeSubtasksById(subtaskId);
        save();
    }
}
