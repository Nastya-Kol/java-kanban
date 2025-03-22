package manager;


import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> historyStorage = new ArrayList<>();

    @Override
    public void addTask(Task task){
        if (Objects.isNull(task)) {
            return;
        }
        historyStorage.add(new Task(task.getId(), task.getName(), task.getDescription(), task.getStatus()));
        if (historyStorage.size()>10) {
            historyStorage.remove(0);
        }
    }

    @Override
    public List<Task> getHistory(){
    return new ArrayList<>(historyStorage);
    }
}
