package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcMpaRepository implements MpaRepository {
    private final NamedParameterJdbcOperations jdbc;

    @Override
    public Optional<Mpa> findById(int id) {
        log.debug("Поиск MPA по ID: {}", id);

        String sql = "SELECT mpa_id, name FROM mpa WHERE mpa_id = :mpa_id";
        MapSqlParameterSource params = new MapSqlParameterSource("mpa_id", id);

        List<Mpa> mpaList = jdbc.query(sql, params, (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        });

        Optional<Mpa> result = mpaList.isEmpty() ? Optional.empty() : Optional.of(mpaList.getFirst());
        log.debug("MPA с ID {} {}", id, result.isPresent() ? "найдено" : "не найдено");
        return result;
    }

    @Override
    public List<Mpa> findAll() {
        log.debug("Получение списка всех MPA");

        String sql = "SELECT mpa_id, name FROM mpa ORDER BY mpa_id";
        List<Mpa> mpaList = jdbc.query(sql, (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        });

        log.debug("Найдено {} MPA рейтингов", mpaList.size());
        return mpaList;
    }
}