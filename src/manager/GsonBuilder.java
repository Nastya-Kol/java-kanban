package manager;

import com.google.gson.Gson;

import java.time.Duration;
import java.time.LocalDateTime;

public final class GsonBuilder {

    private GsonBuilder() {
    }

    public static Gson getGson() {
        return new com.google.gson.GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }
}
