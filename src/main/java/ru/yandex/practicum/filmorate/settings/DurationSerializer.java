package ru.yandex.practicum.filmorate.settings;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Duration;

public class DurationSerializer extends JsonSerializer<Duration> {
    @Override
    public void serialize(Duration duration, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(duration.toMinutes());
    }
}