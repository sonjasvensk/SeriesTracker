package com.seriestracker.web;

import com.seriestracker.domain.Series;
import com.seriestracker.domain.SeriesRepository;
import jakarta.validation.Valid;
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

    public SeriesPageController(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    @GetMapping({"/", "/series", "/series-list"})
    public String listPage(Model model) {
        model.addAttribute("seriesList", seriesRepository.findAll());
        return "series-list";
    }

    @GetMapping("/admin/series")
    public String adminPage(Model model) {
        model.addAttribute("seriesForm", new Series());
        model.addAttribute("seriesList", seriesRepository.findAll());
        return "series-admin";
    }

    @PostMapping("/admin/series")
    public String createSeries(@Valid @ModelAttribute("seriesForm") Series seriesForm,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("seriesList", seriesRepository.findAll());
            return "series-admin";
        }

        seriesRepository.save(seriesForm);
        return "redirect:/admin/series";
    }

    @PostMapping("/admin/series/{id}/delete")
    public String deleteSeries(@PathVariable Long id) {
        seriesRepository.deleteById(id);
        return "redirect:/admin/series";
    }

    @GetMapping("/admin/series/{id}/edit")
    public String editSeries(@PathVariable Long id, Model model) {
        Series series = seriesRepository.findById(id).orElse(null);

        if (series == null) {
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

        seriesForm.setId(id);
        seriesRepository.save(seriesForm);
        return "redirect:/admin/series";
    }
}
