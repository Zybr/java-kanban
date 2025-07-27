package http.serialization.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        String durationStr = String.format(
                "%d:%d:%d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart()
        );
        jsonWriter.value(durationStr);
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        String[] parts = jsonReader.nextString().split(":", 3);
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        int totalSeconds = seconds + minutes * 60 + hours * 60 * 60;

        return Duration.ofSeconds(totalSeconds);
    }
}
