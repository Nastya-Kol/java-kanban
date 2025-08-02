package manager;

import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;
import model.TaskType;

import java.io.File;
import java.io.*;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Новый метод сохранения состояния
    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Записываем заголовок
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            // Сохраняем все задачи
            for (Task task : getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }

            // Сохраняем все эпики
            for (Epic epic : getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }

            // Сохраняем все подзадачи
            for (Subtask subtask : getSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
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

        return String.join(",",
                String.valueOf(task.getId()),
                type.name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                epicId
        );
    }

    // Метод загрузки из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            // Пропускаем заголовок
            for (int i = 1; i < lines.length; i++) {
                Task task = fromString(lines[i]);
                if (task != null) {
                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.subTasks.put(task.getId(), (Subtask) task);
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                }
            }
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
        if (parts.length < 6) {
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            TaskType type = TaskType.valueOf(parts[1].trim());
            String name = parts[2].trim();
            TaskStatus status = TaskStatus.valueOf(parts[3].trim());
            String description = parts[4].trim();
            String epicIdStr = parts[5].trim();

            switch (type) {
                case TASK:
                    return new Task(id, name, description, status);
                case EPIC:
                    return new Epic(id, name, description, status);
                case SUBTASK:
                    int epicId = Integer.parseInt(epicIdStr);
                    return new Subtask(id, name, description, status, epicId);
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
        } catch (NumberFormatException e) {
            throw new ManagerSaveException("Ошибка формата числа в строке: " + value, e);
        } catch (IllegalArgumentException e) {
            throw new ManagerSaveException("Ошибка формата данных в строке: " + value, e);
        }

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
