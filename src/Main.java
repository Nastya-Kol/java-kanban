import manager.InMemoryTaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import manager.TaskManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Task1", "model.Task discription#1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "model.Task discription#2", TaskStatus.IN_PROGRESS);

        Epic epic1 = new Epic("model.Epic #1", "model.Epic discription#1", TaskStatus.NEW);
        Epic epic2 = new Epic("model.Epic #2", "model.Epic discription#2", TaskStatus.NEW);

        Subtask subtask1 = new Subtask("model.Subtask #1", "model.Task discription #1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("model.Subtask #2", "model.Task discription #2", TaskStatus.NEW, epic1.getId());

        manager.saveTasks(task1);
        manager.saveTasks(task2);

        System.out.println(task1);
        System.out.println(task2);

        manager.getTaskById(1);

        task1.setStatus(TaskStatus.DONE);
        manager.updateTasks(task1);
        manager.getTaskById(1);
        System.out.println(task1);

        int epicId1 = manager.saveEpics(epic1);
        manager.saveEpics(epic2);

        System.out.println(epic1);
        System.out.println(epic2);

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


        manager.getEpicById(4);

        manager.getEpicById(4);

        printAllTasks(manager);

    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println();
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getAllSubtaskByEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}