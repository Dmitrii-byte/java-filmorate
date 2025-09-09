package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.filmorate.settings.DurationDeserializer;
import ru.yandex.practicum.filmorate.settings.DurationSerializer;

import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private Long id;
    @NotNull
    private String name;
    private String description;
    private LocalDate releaseDate;

    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration duration;
}
