package com.seriestracker.web;

import com.seriestracker.domain.Series;
import com.seriestracker.domain.SeriesRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SeriesPageController {

    private final SeriesRepository seriesRepository;

    public SeriesPageController(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    @GetMapping({"/", "/series"})
    public String listPage(Model model) {
        model.addAttribute("seriesForm", new Series());
        model.addAttribute("seriesList", seriesRepository.findAll());
        return "series-list";
    }

    @PostMapping("/series")
    public String createSeries(@Valid @ModelAttribute("seriesForm") Series seriesForm,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("seriesList", seriesRepository.findAll());
            return "series-list";
        }

        seriesRepository.save(seriesForm);
        return "redirect:/series";
    }
}
