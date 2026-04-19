package com.seriestracker.web;

import com.seriestracker.domain.Series;
import com.seriestracker.domain.SeriesRepository;
import com.seriestracker.domain.Tag;
import com.seriestracker.domain.TagRepository;
import com.seriestracker.domain.User;
import com.seriestracker.domain.UserRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public SeriesController(SeriesRepository seriesRepository, TagRepository tagRepository, UserRepository userRepository) {
        this.seriesRepository = seriesRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return userRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<Series> findAll() {
        User user = getCurrentUser();
        return user != null ? seriesRepository.findByUser(user) : List.of();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public Series create(@Valid @RequestBody Series series) {
        User user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not authenticated");
        }
        series.setUser(user);
        series.setTagEntities(resolveTagEntities(series.getTags()));
        return seriesRepository.save(series);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<Series> findById(@PathVariable Long id) {
        User user = getCurrentUser();
        return seriesRepository.findById(id)
                .filter(series -> user != null && series.getUser().getId().equals(user.getId()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<Series> update(@PathVariable Long id, @Valid @RequestBody Series update) {
        User user = getCurrentUser();
        return seriesRepository.findById(id)
                .filter(series -> user != null && series.getUser().getId().equals(user.getId()))
                .map(existing -> {
                    existing.setTitle(update.getTitle());
                    existing.setPlatform(update.getPlatform());
                    existing.setGenre(update.getGenre());
                    existing.setTags(update.getTags());
                    existing.setTagEntities(resolveTagEntities(update.getTags()));
                    existing.setRating(update.getRating());
                    existing.setComment(update.getComment());
                    return ResponseEntity.ok(seriesRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = getCurrentUser();
        Optional<Series> series = seriesRepository.findById(id);
        
        if (series.isPresent() && user != null && series.get().getUser().getId().equals(user.getId())) {
            seriesRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.notFound().build();
    }

    private Set<Tag> resolveTagEntities(String tagsInput) {
        return List.of(tagsInput.split(","))
                .stream()
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toMap(
                        tag -> tag.toLowerCase(Locale.ROOT),
                        tag -> tag,
                        (first, second) -> first
                ))
                .values()
                .stream()
                .map(tag -> tagRepository.findByNameIgnoreCase(tag)
                        .orElseGet(() -> tagRepository.save(new Tag(tag))))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}
