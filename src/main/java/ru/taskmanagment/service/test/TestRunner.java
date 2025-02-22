package ru.taskmanagment.service.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
public class TestRunner {

    private static final double COVERAGE_THRESHOLD = 80.0;

    public boolean runTests(Path tempDir) {
        log.info("Running tests and generating coverage report...");

        Path pomFile = findPomFile(tempDir);
        if (pomFile == null) {
            log.error("No pom.xml found in the extracted files.");
            return false;
        }

        try {
            // Exécute les tests et génère le rapport de couverture
            ProcessBuilder processBuilder = new ProcessBuilder("mvn", "clean", "test", "jacoco:report");
            processBuilder.directory(pomFile.getParent().toFile());
            Process process = processBuilder.start();
            captureProcessOutput(process);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Tests failed with exit code: {}", exitCode);
                return false;
            }

            // Vérification du taux de couverture
            double coverage = getTestCoverage(pomFile.getParent());
            if (coverage < COVERAGE_THRESHOLD) {
                log.error("Test coverage is below threshold: {}% (Required: {}%)", coverage, COVERAGE_THRESHOLD);
                return false;
            }

            log.info("Tests passed successfully with {}% coverage.", coverage);
            return true;
        } catch (InterruptedException | IOException e) {
            log.error("Error during test execution: {}", e.getMessage(), e);
            return false;
        }
    }

    private Path findPomFile(Path tempDir) {
        try {
            return Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("pom.xml"))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            log.error("Error finding pom.xml: {}", e.getMessage(), e);
            return null;
        }
    }

    private void captureProcessOutput(Process process) {
        try (BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = stdOutput.readLine()) != null) {
                log.info(line);
            }
            while ((line = stdError.readLine()) != null) {
                log.error(line);
            }
        } catch (IOException e) {
            log.error("Error capturing process output: {}", e.getMessage(), e);
        }
    }

    private double getTestCoverage(Path projectDir) {
        Path reportFile = projectDir.resolve("target/site/jacoco/index.html");
        if (!Files.exists(reportFile)) {
            log.error("JaCoCo report not found at {}", reportFile);
            return 0.0;
        }

        try {
            String content = Files.readString(reportFile);
            String coverageValue = content.split("Total</td>")[1].split("<td class=\"ctr2\">")[1].split("</td>")[0];
            return Double.parseDouble(coverageValue.replace("%", "").trim());
        } catch (Exception e) {
            log.error("Error parsing JaCoCo report: {}", e.getMessage(), e);
            return 0.0;
        }
    }
}
