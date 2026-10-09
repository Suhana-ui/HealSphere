package com.healsphere.controller;

import com.healsphere.service.EnvironmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Serves the HTML pages. Each method returns the name of a Thymeleaf template. */
@Controller
public class PageController {

    private final EnvironmentService environmentService;

    public PageController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("environments", environmentService.getAll());
        return "index";
    }

    @GetMapping("/environments")
    public String environments() {
        return "redirect:/#environments";
    }

    @GetMapping("/about")
    public String about() {
        return "redirect:/#about";
    }

    @GetMapping("/session")
    public String session(@RequestParam(name = "env", required = false) String env, Model model) {
        model.addAttribute("env", environmentService.resolve(env)); // invalid/missing -> forest
        model.addAttribute("environments", environmentService.getAll());
        return "session";
    }

    @GetMapping("/progress")
    public String progress() {
        return "progress";
    }
}
