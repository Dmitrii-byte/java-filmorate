package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaRepository;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Validated
@Slf4j
@RequiredArgsConstructor
public class MpaController {
    private final MpaRepository mpaRepository;

    @GetMapping
    public List<Mpa> findAll() {
        log.info("GET /mpa - получение всех рейтингов MPA");
        return mpaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mpa findById(@PathVariable int id) {
        log.info("GET /mpa/{} - получение рейтинга MPA по ID", id);
        return mpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + id + " не найден"));
    }
}