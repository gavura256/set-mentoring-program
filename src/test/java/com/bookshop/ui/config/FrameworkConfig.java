package com.bookshop.ui.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "framework")
public class FrameworkConfig {

    private String baseUrl;
    private boolean headless;
    private Duration defaultTimeout;
    private int slowMotionMs;
    private String screenshotsDir;
    private String tracesDir;
    private String adminEmail;
    private String adminPassword;
    private String managerEmail;
    private String managerPassword;
}
