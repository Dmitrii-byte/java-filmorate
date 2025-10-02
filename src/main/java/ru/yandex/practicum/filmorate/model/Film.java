package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.settings.DurationDeserializer;
import ru.yandex.practicum.filmorate.settings.DurationSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    @NotBlank(message = "Название фильма не может быть пустым")
    @NotNull
    private String name;

    @Size(max = 200, message = "Описание фильма не может превышать 200 символов")
    @NotBlank(message = "Описание фильма не может быть пустым")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;
    private Set<Long> likes = new HashSet<>();

    @NotNull(message = "Продолжительность обязательна")
    @JsonSerialize(using = DurationSerializer.class)
    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration duration;
}
