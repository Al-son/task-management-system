package ru.taskmanagment.service.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
@Slf4j
public class SensitiveDataChecker {

    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
            // Clés génériques (AWS, Google, Azure, API Keys)
            Pattern.compile("(?i)(aws_access_key_id|aws_secret_access_key|api_key|secret_key|access_key|private_key|client_secret)[\\s:=]+[\"']?[A-Za-z0-9+/=_-]{16,}[\"']?"),

            // Clés AWS
            Pattern.compile("(?i)AKIA[0-9A-Z]{16}"), // AWS Access Key ID
            Pattern.compile("(?i)aws_secret_access_key[\\s:=]+[\"']?[A-Za-z0-9/+=]{40}[\"']?"), // AWS Secret Key

            // Clés Google Cloud
            Pattern.compile("(?i)AIza[0-9A-Za-z-_]{35}"), // Google API Key
            Pattern.compile("(?i)ya29\\.[0-9A-Za-z-_]+"), // Google OAuth Token

            // Clés Azure
            Pattern.compile("(?i)eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+"), // JWT Token
            Pattern.compile("(?i)DefaultEndpointsProtocol=https;AccountName=[a-z0-9]+;AccountKey=[A-Za-z0-9+/=]+"), // Azure Storage Key

            // Clés Facebook / Twitter / Slack
            Pattern.compile("(?i)(facebook|twitter|slack|github)_token[\\s:=]+[\"']?[A-Za-z0-9-_]{16,}[\"']?"),

            // Jetons JWT (JSON Web Tokens)
            Pattern.compile("(?i)eyJ[a-zA-Z0-9]{30,}\\.[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+"),

            // Clés MongoDB / MySQL / PostgreSQL
            Pattern.compile("(?i)mongodb\\+srv://[a-zA-Z0-9_-]+:[a-zA-Z0-9!@#$%^&*()-_=+]+@"),
            Pattern.compile("(?i)jdbc:mysql://[a-zA-Z0-9_-]+:[a-zA-Z0-9!@#$%^&*()-_=+]+@"),
            Pattern.compile("(?i)jdbc:postgresql://[a-zA-Z0-9_-]+:[a-zA-Z0-9!@#$%^&*()-_=+]+@"),

            // Clés Stripe
            Pattern.compile("(?i)sk_live_[0-9a-zA-Z]{24}"),
            Pattern.compile("(?i)pk_live_[0-9a-zA-Z]{24}"),

            // Clés GitHub / GitLab
            Pattern.compile("(?i)ghp_[A-Za-z0-9]{36}"), // GitHub Token
            Pattern.compile("(?i)glpat-[A-Za-z0-9-_]{20,}"), // GitLab Token

            // Clés PayPal / Twilio
            Pattern.compile("(?i)access_token[\\s:=]+[\"']?A21AA[A-Za-z0-9_-]{60,}[\"']?"),
            Pattern.compile("(?i)twilio_account_sid[\\s:=]+[\"']?AC[a-zA-Z0-9]{32}[\"']?"),
            Pattern.compile("(?i)twilio_auth_token[\\s:=]+[\"']?[a-zA-Z0-9]{32}[\"']?")
    );

    public boolean checkSensitiveData(Path projectRoot) {
        log.info("Scanning for sensitive data in project: {}", projectRoot);

        try (Stream<Path> paths = Files.walk(projectRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".env") ||
                            path.toString().endsWith(".yml") || path.toString().endsWith(".json") ||
                            path.toString().endsWith(".xml") || path.toString().endsWith(".properties"))
                    .allMatch(this::scanFileForSensitiveData);
        } catch (IOException e) {
            log.error("Error while scanning for sensitive data", e);
            return false;
        }
    }

    private boolean scanFileForSensitiveData(Path filePath) {
        log.info("🔍 Scanning file: {}", filePath);

        try (Stream<String> lines = Files.lines(filePath)) {
            return lines.noneMatch(line -> {
                boolean found = containsSensitiveData(line);
                if (found) {
                    log.warn("🚨 Sensitive data found in file: {}", filePath);
                }
                return found;
            });
        } catch (IOException e) {
            log.warn("⚠️ Could not read file: {}", filePath, e);
            return true;
        }
    }


    private boolean containsSensitiveData(String line) {
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                log.warn("⚠️ Potential sensitive data found: {}", line);
                return true;
            }
        }
        return false;
    }
}
