public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager manager = new TaskManager();
        Task task1 = new Task("Task1", "Task discription#1", TaskStatus.NEW);
        Task task2 = new Task("Task2", "Task discription#2", TaskStatus.IN_PROGRESS);

        task1 = manager.saveTasks(task1);
        task2 = manager.saveTasks(task2);
        System.out.println(task1);
        System.out.println(task2);

        Epic epic1 = new Epic("Epic #1", "Epic discription#1", TaskStatus.NEW);
        Epic epic2 = new Epic("Epic #2", "Epic discription#2", TaskStatus.NEW);
        epic1 = manager.saveEpics(epic1);
        epic2 = manager.saveEpics(epic2);
        System.out.println(epic1);
        System.out.println(epic2);

        Subtask subtask1 = new Subtask("Subtask #1", "Task discription #1", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask #2", "Task discription #2", TaskStatus.NEW, epic1.getId());
        subtask1 = manager.saveSubtasks(subtask1);
        subtask2 = manager.saveSubtasks(subtask2);

        System.out.println(subtask1);
        System.out.println(subtask2);

        subtask1.setStatus(TaskStatus.DONE);
        subtask1 = manager.updateSubtasks(subtask1);
        System.out.println(subtask1);
        subtask2.setStatus(TaskStatus.DONE);
        subtask2 = manager.updateSubtasks(subtask2);
        System.out.println(subtask2);

        System.out.println(epic1);

        System.out.println("\n" + manager.getEpics());
        System.out.println("\n" + manager.getTasks());
        System.out.println("удаляем 5 сабтаск");
        manager.removeTasksById(5);
        System.out.println("выводим что получилось");
        System.out.println("\n" + manager.getEpics());
        System.out.println("\n" + manager.getTasks());

    }
}
