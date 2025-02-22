//package ru.taskmanagment.service.test;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//
//@Component
//@Slf4j
//public class StaticCodeAnalyzer {
//
//    public boolean analyzeCode() {
//        log.info("Running static code analysis...");
//        try {
//            Process process = Runtime.getRuntime().exec("mvn com.github.spotbugs:spotbugs-maven-plugin:check");
//            // Capture standard output
//            BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            // Capture error output
//            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//
//            String line;
//            while ((line = stdOutput.readLine()) != null) {
//                log.info(line);
//            }
//            while ((line = stdError.readLine()) != null) {
//                log.error(line);
//            }
//
//            int exitCode = process.waitFor();
//            if (exitCode != 0) {
//                log.error("Static code analysis failed with exit code: {}", exitCode);
//                return false;
//            }
//            return true;
//        } catch (Exception e) {
//            log.error("Error during static code analysis: {}", e.getMessage(), e);
//            return false;
//        }
//    }
//}

package ru.taskmanagment.service.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.json.JSONObject;
import java.util.Map;


@Component
@Slf4j
public class StaticCodeAnalyzer {

    // Règles spécifiques aux fichiers Java
    private static final List<String> JAVA_BAD_PATTERNS = List.of(
            "System.out.println",
            "Runtime.getRuntime().exec",
            "java.lang.reflect",
            "eval(",
            "Thread.sleep(",
            "new File(",
            ".wait(", ".notify(",
            "setAccessible(true)"
    );

    // Règles spécifiques aux fichiers JavaScript
    private static final List<String> JS_BAD_PATTERNS = List.of(
            "eval(",
            "document.write(",
            "innerHTML =",
            "setTimeout(", "setInterval(",
            "fetch(", "XMLHttpRequest(",
            "localStorage.setItem(", "sessionStorage.setItem(",
            "Function("
    );

    public boolean analyzeCode(Path projectPath) {
        log.info("🔍 Début de l'analyse statique du code dans {}", projectPath);

        try (Stream<Path> paths = Files.walk(projectPath)) {
            List<Path> sourceFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".js"))
                    .toList();

            boolean hasIssues = false;

            for (Path file : sourceFiles) {
                log.info("📄 Analyse du fichier : {}", file);
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();

                    if (file.toString().endsWith(".java")) {
                        for (String pattern : JAVA_BAD_PATTERNS) {
                            if (line.contains(pattern)) {
                                log.warn("⚠ Problème Java détecté dans {} à la ligne {} : {}", file, i + 1, pattern);
                                hasIssues = true;
                            }
                        }
                    }

                    if (file.toString().endsWith(".js")) {
                        for (String pattern : JS_BAD_PATTERNS) {
                            if (line.contains(pattern)) {
                                log.warn("⚠ Problème JavaScript détecté dans {} à la ligne {} : {}", file, i + 1, pattern);
                                hasIssues = true;
                            }
                        }
                    }
                }
            }

            // Vérification des bibliothèques utilisées
            hasIssues |= checkDependencies(projectPath);

            if (!hasIssues) {
                log.info("✅ Aucun problème détecté.");
                return true;
            } else {
                log.warn("❌ Des problèmes ont été détectés !");
                return false;
            }
        } catch (IOException e) {
            log.error("❌ Erreur lors de l'analyse statique : {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean checkDependencies(Path projectPath) {
        boolean hasIssues = false;

        try {
            // Vérification des dépendances Maven (pom.xml)
            Path pomPath = projectPath.resolve("pom.xml");
            if (Files.exists(pomPath)) {
                hasIssues |= analyzeMavenDependencies(pomPath);
            }

            // Vérification des dépendances Node.js (package.json)
            Path packageJsonPath = projectPath.resolve("package.json");
            if (Files.exists(packageJsonPath)) {
                hasIssues |= analyzeNodeDependencies(packageJsonPath);
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'analyse des dépendances : {}", e.getMessage(), e);
        }

        return hasIssues;
    }

    private boolean analyzeMavenDependencies(Path pomPath) {
        log.info("🔍 Analyse des dépendances Maven dans {}", pomPath);
        boolean hasIssues = false;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(Files.newInputStream(pomPath));

            NodeList dependencies = document.getElementsByTagName("dependency");
            for (int i = 0; i < dependencies.getLength(); i++) {
                String groupId = dependencies.item(i).getChildNodes().item(1).getTextContent();
                String artifactId = dependencies.item(i).getChildNodes().item(3).getTextContent();
                String version = dependencies.item(i).getChildNodes().item(5).getTextContent();

                log.info("📦 Dépendance trouvée : {}:{}:{}", groupId, artifactId, version);

                if (isVulnerableLibrary(artifactId, version)) {
                    log.warn("⚠ Bibliothèque Maven vulnérable détectée : {}:{}:{}", groupId, artifactId, version);
                    hasIssues = true;
                }
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'analyse du pom.xml : {}", e.getMessage(), e);
        }

        return hasIssues;
    }

    private boolean analyzeNodeDependencies(Path packageJsonPath) {
        log.info("🔍 Analyse des dépendances Node.js dans {}", packageJsonPath);
        boolean hasIssues = false;

        try {
            String content = new String(Files.readAllBytes(packageJsonPath));
            JSONObject json = new JSONObject(content);
            JSONObject dependencies = json.getJSONObject("dependencies");

            for (Iterator it = dependencies.keys(); it.hasNext(); ) {
                String packageName = (String) it.next();
                String version = dependencies.getString(packageName);
                log.info("📦 Dépendance trouvée : {}:{}", packageName, version);

                if (isVulnerableLibrary(packageName, version)) {
                    log.warn("⚠ Bibliothèque Node.js vulnérable détectée : {}:{}", packageName, version);
                    hasIssues = true;
                }
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'analyse du package.json : {}", e.getMessage(), e);
        }

        return hasIssues;
    }

    private boolean isVulnerableLibrary(String library, String version) {
        // Simule une base de données de bibliothèques vulnérables
        Map<String, List<String>> vulnerableLibs = Map.of(
                "log4j-core", List.of("2.14.0", "2.15.0"),
                "express", List.of("4.17.1")
        );

        return vulnerableLibs.getOrDefault(library, Collections.emptyList()).contains(version);
    }
}
