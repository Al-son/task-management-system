package ru.taskmanagment.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.taskmanagment.service.test.DependencyChecker;
import ru.taskmanagment.service.test.SensitiveDataChecker;
import ru.taskmanagment.service.test.StaticCodeAnalyzer;
import ru.taskmanagment.service.test.TestRunner;

@Configuration
public class ServiceConfig {

    @Bean
    public DependencyChecker dependencyChecker() {
        return new DependencyChecker();
    }

    @Bean
    public StaticCodeAnalyzer staticCodeAnalyzer() {
        return new StaticCodeAnalyzer();
    }

    @Bean
    public SensitiveDataChecker sensitiveDataChecker() {
        return new SensitiveDataChecker();
    }

    @Bean
    public TestRunner testRunner() {
        return new TestRunner();
    }
}