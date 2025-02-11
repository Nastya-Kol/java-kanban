import java.util.List;
import java.util.ArrayList;

public class Epic extends Task {

    private final List<Integer> subTaskIds = new ArrayList<>();

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
    }

    public List<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void removeSubtaskId(int subtaskId) {
        subTaskIds.remove((Integer) subtaskId);
    }


    @Override
    public String toString() {
        return "Epic{" +
                "subTaskIds=" + subTaskIds +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}