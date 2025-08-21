package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private final int epicId;

    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpic() {
        return epicId;
    }

    @Override
    public String toString() {
        return "model.Subtask{" +
                "epic=" + epicId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }
}