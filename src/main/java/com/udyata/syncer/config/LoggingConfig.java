package com.udyata.syncer.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LoggingConfig {

    @PostConstruct
    public void init() throws IOException {

        Path logsDir = Paths.get("logs");
        if (!Files.exists(logsDir)) {
            Files.createDirectories(logsDir);
        }
    }
}