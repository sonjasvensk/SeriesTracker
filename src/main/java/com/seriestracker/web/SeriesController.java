package com.seriestracker.web;

import com.seriestracker.domain.Series;
import com.seriestracker.domain.SeriesRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesRepository seriesRepository;

    public SeriesController(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    @GetMapping
    public List<Series> findAll() {
        return seriesRepository.findAll();
    }

    @PostMapping
    public Series create(@Valid @RequestBody Series series) {
        return seriesRepository.save(series);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Series> findById(@PathVariable Long id) {
        return seriesRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Series> update(@PathVariable Long id, @Valid @RequestBody Series update) {
        return seriesRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(update.getTitle());
                    existing.setPlatform(update.getPlatform());
                    existing.setGenre(update.getGenre());
                    existing.setTags(update.getTags());
                    existing.setRating(update.getRating());
                    return ResponseEntity.ok(seriesRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!seriesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        seriesRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
