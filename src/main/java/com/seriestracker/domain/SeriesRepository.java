package com.seriestracker.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {
    List<Series> findByUser(User user);
}
