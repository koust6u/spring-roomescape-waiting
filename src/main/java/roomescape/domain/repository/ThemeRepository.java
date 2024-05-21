package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import roomescape.domain.Theme;

public interface ThemeRepository extends Repository<Theme, Long> {
    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    @Query(value = """
             SELECT theme.id, theme.name, theme.description, theme.thumbnail
                            FROM reservation
                            LEFT JOIN theme ON theme.id=reservation.theme_id
                            WHERE reservation.date >= ? AND reservation.date <= ?
                            GROUP BY theme.id
                            ORDER BY COUNT(*) DESC
                            LIMIT ?;
            """, nativeQuery = true)
    List<Theme> findThemeByPeriodWithLimit(LocalDate start, LocalDate end, int limit);

    List<Theme> findAll();

    void delete(Theme theme);

    void deleteAll();
}
