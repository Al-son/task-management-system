package ru.taskmanagment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MergeTestService {

    private final RestTemplate restTemplate;

    public String runTestsAfterMerge(String repoPath, String strategy) {
        // Trigger the test run (local or CI/CD)
        String testRunResult = triggerTestExecution(repoPath);
        if (testRunResult.contains("Test execution failed")) {
            return testRunResult;
        }
        // After the test execution, collect the results
        return analyzeTestResults(repoPath);
    }

    private String triggerTestExecution(String repoPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mvn", "test");  // For Maven, adjust as needed
            processBuilder.directory(new File(repoPath)); // Ensure to set the correct directory for the repo
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "Test execution completed successfully!";
            } else {
                return "Test execution failed. Please check the test logs.";
            }
        } catch (IOException | InterruptedException e) {
            return "Test execution failed: " + e.getMessage();
        }
    }

    private String analyzeTestResults(String repoPath) {
        // After running the tests, parse the generated test reports
        File reportFile = new File(repoPath + "/target/surefire-reports/TEST-*.xml");  // For Maven
        if (!reportFile.exists()) {
            return "No test reports found. Please ensure tests are properly configured.";
        }

        // Parse the test result (XML format, use your preferred method)
        StringBuilder testSummary = new StringBuilder("Test Summary:\n");
        try {
            List<String> failedTests = new ArrayList<>();
            Files.walk(reportFile.toPath())
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            // Parse the file to get the failed tests (simplified example, adjust for your format)
                            List<String> lines = Files.readAllLines(file);
                            for (String line : lines) {
                                if (line.contains("<failure>")) {
                                    failedTests.add(line);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            if (failedTests.isEmpty()) {
                testSummary.append("All tests passed!\n");
            } else {
                testSummary.append(failedTests.size()).append(" test(s) failed:\n");
                testSummary.append(failedTests.stream().collect(Collectors.joining("\n")));
            }
        } catch (IOException e) {
            return "Error reading test reports: " + e.getMessage();
        }
        return testSummary.toString();
    }

    // You can add methods to trigger CI/CD pipelines, for example, GitHub Actions or Jenkins
    public void triggerCICDPipeline(String repoName, String branch) {
        String url = "https://api.github.com/repos/YOUR_USERNAME/" + repoName + "/actions/workflows/test.yml/dispatches";
        String body = "{ \"ref\": \"" + branch + "\" }";

        // Setup REST call or use an HTTP client to trigger CI/CD pipeline (GitHub Actions in this example)
        restTemplate.postForObject(url, body, String.class);
    }
}
