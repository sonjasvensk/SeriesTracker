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
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SeriesPageController {

    private final SeriesRepository seriesRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public SeriesPageController(SeriesRepository seriesRepository, TagRepository tagRepository, UserRepository userRepository) {
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

    @GetMapping({"/", "/series", "/series-list"})
    public String listPage(Model model) {
        model.addAttribute("seriesList", seriesRepository.findAll());
        return "series-list";
    }

    @GetMapping("/admin/series")
    public String adminPage(Model model) {
        User user = getCurrentUser();
        model.addAttribute("seriesForm", new Series());
        model.addAttribute("seriesList", user != null ? seriesRepository.findByUser(user) : List.of());
        return "series-admin";
    }

    @PostMapping("/admin/series")
    public String createSeries(@Valid @ModelAttribute("seriesForm") Series seriesForm,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            User user = getCurrentUser();
            model.addAttribute("seriesList", user != null ? seriesRepository.findByUser(user) : List.of());
            return "series-admin";
        }

        User user = getCurrentUser();
        if (user != null) {
            seriesForm.setUser(user);
            seriesForm.setTagEntities(resolveTagEntities(seriesForm.getTags()));
            seriesRepository.save(seriesForm);
        }
        return "redirect:/admin/series";
    }

    @PostMapping("/admin/series/{id}/delete")
    public String deleteSeries(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user != null) {
            seriesRepository.findById(id).ifPresent(series -> {
                if (series.getUser().getId().equals(user.getId())) {
                    seriesRepository.deleteById(id);
                }
            });
        }
        return "redirect:/admin/series";
    }

    @GetMapping("/admin/series/{id}/edit")
    public String editSeries(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        Series series = seriesRepository.findById(id).orElse(null);

        if (series == null || user == null || !series.getUser().getId().equals(user.getId())) {
            return "redirect:/admin/series";
        }

        model.addAttribute("seriesForm", series);
        return "series-edit";
    }

    @PostMapping("/admin/series/{id}/edit")
    public String updateSeries(@PathVariable Long id,
                               @Valid @ModelAttribute("seriesForm") Series seriesForm,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "series-edit";
        }

        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/admin/series";
        }

        Series existing = seriesRepository.findById(id).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return "redirect:/admin/series";
        }

        seriesForm.setId(id);
        seriesForm.setUser(user);
        seriesForm.setTagEntities(resolveTagEntities(seriesForm.getTags()));
        seriesRepository.save(seriesForm);
        return "redirect:/admin/series";
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
