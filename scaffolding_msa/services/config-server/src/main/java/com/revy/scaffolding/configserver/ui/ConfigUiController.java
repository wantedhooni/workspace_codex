package com.revy.scaffolding.configserver.ui;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConfigUiController {
    private final EnvironmentRepository environmentRepository;

    public ConfigUiController(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
    }

    @GetMapping("/config-ui")
    public String index(
        @RequestParam(required = false) String application,
        @RequestParam(required = false) String profile,
        @RequestParam(required = false) String label,
        Model model
    ) {
        ConfigQuery query = new ConfigQuery(
            normalize(application),
            normalize(profile),
            normalize(label)
        );
        model.addAttribute("query", query);
        model.addAttribute("hasResult", false);

        if (StringUtils.hasText(application) && StringUtils.hasText(profile)) {
            Environment environment = environmentRepository.findOne(
                application.trim(),
                profile.trim(),
                StringUtils.hasText(label) ? label.trim() : null
            );
            model.addAttribute("environment", environment);
            model.addAttribute("hasResult", true);
        }

        return "config-ui";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}

