import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager manager = new TaskManager();
        Task task1 = new Task("Task1", "model.Task discription#1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "model.Task discription#2", TaskStatus.IN_PROGRESS);

        manager.saveTasks(task1);
        manager.saveTasks(task2);

        System.out.println(task1);
        System.out.println(task2);

        manager.updateTasks(task1);
        System.out.println(task1);

        Epic epic1 = new Epic("model.Epic #1", "model.Epic discription#1", TaskStatus.NEW);
        Epic epic2 = new Epic("model.Epic #2", "model.Epic discription#2", TaskStatus.NEW);

        int epicId1 = manager.saveEpics(epic1);
        manager.saveEpics(epic2);

        System.out.println(epic1);
        System.out.println(epic2);

        Subtask subtask1 = new Subtask("model.Subtask #1", "model.Task discription #1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("model.Subtask #2", "model.Task discription #2", TaskStatus.NEW, epic1.getId());

        manager.saveSubtasks(subtask1);
        manager.saveSubtasks(subtask2);

        System.out.println(subtask1);
        System.out.println(subtask2);

        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtasks(subtask1);
        System.out.println(subtask1);
        subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtasks(subtask2);
        System.out.println(subtask1);

        System.out.println(epic1);
        System.out.println();

        System.out.println("выводим подзадачи 3-его эпика");
        System.out.println(manager.getAllSubtaskByEpic(epicId1));

        System.out.println("\n" + "Удаляем подзадачу по id");
        manager.removeSubtasksById(5);
        System.out.println("выводим подзадачи 3-его эпика после удаления 1 подзадачи");
        System.out.println(manager.getAllSubtaskByEpic(epicId1));
        System.out.println("\n" + "Удаляем эпик по id");
        manager.removeEpicsById(3);
        System.out.println("\n" + "Получаем все эпики");
        System.out.println("\n" + manager.getEpics());

        System.out.println("\n" + manager.getTasks());

        System.out.println("\n" + "Удаляем задачу по id");
        manager.removeTasksById(1);
        System.out.println("\n" + "Пытаемся получить удаленную задачу 1 по id");
        System.out.println(manager.getTaskById(1));

        manager.deleteAllSubtasks(3);
        System.out.println("выводим что получилось");
        System.out.println("\n" + manager.getEpics());
        System.out.println("\n" + manager.getTasks());

        System.out.println("Создаем подзадачу несуществующего эпика");
        Subtask subtask3 = new Subtask("model.Subtask #2", "model.Task discription #2", TaskStatus.NEW, 8);
        manager.saveSubtasks(subtask3);
    }
}