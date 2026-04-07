package com.seriestracker.web;

import com.seriestracker.domain.SeriesRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SeriesPageController {

    private final SeriesRepository seriesRepository;

    public SeriesPageController(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    @GetMapping({"/", "/series"})
    public String listPage(Model model) {
        model.addAttribute("seriesList", seriesRepository.findAll());
        return "series-list";
    }
}
