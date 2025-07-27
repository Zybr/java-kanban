package http.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.serialization.adapters.DurationAdapter;
import http.serialization.adapters.LocalDateTimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

public class SerializerFactory {
    private static Gson gson;

    public static Gson getSerializer() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .create();
        }

        return gson;
    }
}
