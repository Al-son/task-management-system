package ru.taskmanagment.service.test;

import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class DependencyChecker {

    private final WebClient webClient;

    // Configuration Nexus
    private static final String NEXUS_URL = "http://nexus.local/rest/vulnerability";
    private static final String NEXUS_USERNAME = "admin";
    private static final String NEXUS_PASSWORD = "admin123";


    private static final Map<String, String> knownVulnerabilities = new HashMap<>();

    static {
        knownVulnerabilities.put("org.springframework:spring-core", "5.3.10");
        knownVulnerabilities.put("com.fasterxml.jackson.core:jackson-databind", "2.12.3");
        knownVulnerabilities.put("org.apache.commons:commons-lang3", "3.10");
    }

    public boolean checkDependencies() {
        log.info("🔍 Vérification des dépendances dans pom.xml...");
        File pomFile = new File("pom.xml");

        if (!pomFile.exists()) {
            log.error("❌ Aucun fichier pom.xml trouvé !");
            return false;
        }

        Map<String, String> dependencies = extractDependencies(pomFile);
        List<Map<String, String>> vulnerableDependencies = new ArrayList<>();

        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            String groupIdArtifactId = entry.getKey();
            String version = entry.getValue();

            if (knownVulnerabilities.containsKey(groupIdArtifactId)) {
                String vulnerableVersion = knownVulnerabilities.get(groupIdArtifactId);

                if (isVulnerable(version, vulnerableVersion)) {
                    log.warn("⚠️ DÉPENDANCE VULNÉRABLE DÉTECTÉE : {} version {} (corrigée après {})",
                            groupIdArtifactId, version, vulnerableVersion);

                    Map<String, String> reportEntry = new HashMap<>();
                    reportEntry.put("Dépendance", groupIdArtifactId);
                    reportEntry.put("Version actuelle", version);
                    reportEntry.put("Version corrigée", vulnerableVersion);
                    vulnerableDependencies.add(reportEntry);
                }
            }
        }

        generateJsonReport(vulnerableDependencies);
        generateHtmlReport(vulnerableDependencies);

        return vulnerableDependencies.isEmpty();
    }

    private Map<String, String> extractDependencies(File pomFile) {
        Map<String, String> dependencies = new HashMap<>();
        try {
            log.info("📄 Analyse du fichier pom.xml pour extraire les dépendances...");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pomFile);
            document.getDocumentElement().normalize();

            NodeList dependencyNodes = document.getElementsByTagName("dependency");
            log.info("🔢 {} dépendances trouvées dans le pom.xml", dependencyNodes.getLength());

            for (int i = 0; i < dependencyNodes.getLength(); i++) {
                Element dependency = (Element) dependencyNodes.item(i);
                String groupId = getTagValue("groupId", dependency);
                String artifactId = getTagValue("artifactId", dependency);
                String version = getTagValue("version", dependency);

                if (groupId != null && artifactId != null) {
                    String depKey = groupId + ":" + artifactId;

                    if (version == null || version.isEmpty()) {
                        log.warn("⚠️ Dépendance détectée sans version : {}", depKey);
                        continue; // Ignore dependencies without versions
                    }

                    dependencies.put(depKey, version);
                    log.debug("📦 Dépendance détectée : {} - Version {}", depKey, version);
                }
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'analyse du pom.xml : {}", e.getMessage(), e);
        }
        return dependencies;
    }


    private boolean isVulnerable(String currentVersion, String vulnerableVersion) {
        if (currentVersion == null || currentVersion.isEmpty()) {
            log.warn("⚠️ Impossible de vérifier la vulnérabilité : version actuelle inconnue.");
            return false; // Ignore dependencies without versions
        }
        return compareVersions(currentVersion, vulnerableVersion) <= 0;
    }


    private int compareVersions(String v1, String v2) {
        if (v1 == null || v1.isEmpty() || v2 == null || v2.isEmpty()) {
            log.error("❌ Comparaison de versions impossible : v1 = '{}', v2 = '{}'", v1, v2);
            return 1; // Assume the current version is not vulnerable if unknown
        }

        String[] v1Parts = v1.split("\\.");
        String[] v2Parts = v2.split("\\.");
        int length = Math.max(v1Parts.length, v2Parts.length);

        for (int i = 0; i < length; i++) {
            int num1 = (i < v1Parts.length) ? parseIntSafe(v1Parts[i]) : 0;
            int num2 = (i < v2Parts.length) ? parseIntSafe(v2Parts[i]) : 0;
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }

    private int parseIntSafe(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            log.warn("⚠️ Impossible de parser le numéro de version : '{}'", str);
            return 0; // Assume unknown version numbers are the lowest
        }
    }


    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return null;
    }

    private void generateJsonReport(List<Map<String, String>> vulnerabilities) {
        File jsonFile = new File("dependency-report.json");
        try (FileWriter writer = new FileWriter(jsonFile)) {
            writer.write("[\n");
            for (int i = 0; i < vulnerabilities.size(); i++) {
                Map<String, String> entry = vulnerabilities.get(i);
                writer.write(String.format("  {\"Dépendance\": \"%s\", \"Version actuelle\": \"%s\", \"Version corrigée\": \"%s\"}",
                        entry.get("Dépendance"), entry.get("Version actuelle"), entry.get("Version corrigée")));
                if (i < vulnerabilities.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("]");
            log.info("📄 Rapport JSON généré : {}", jsonFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("❌ Erreur lors de la génération du rapport JSON : {}", e.getMessage());
        }
    }

    private void generateHtmlReport(List<Map<String, String>> vulnerabilities) {
        File htmlFile = new File("dependency-report.html");
        try (FileWriter writer = new FileWriter(htmlFile)) {
            writer.write("<html><head><title>Rapport des Dépendances</title></head><body>");
            writer.write("<h2>🔍 Rapport des Dépendances Vulnérables</h2>");
            writer.write("<table border='1'><tr><th>Dépendance</th><th>Version Actuelle</th><th>Version Corrigée</th></tr>");

            for (Map<String, String> entry : vulnerabilities) {
                writer.write(String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>",
                        entry.get("Dépendance"), entry.get("Version actuelle"), entry.get("Version corrigée")));
            }
            writer.write("</table></body></html>");
            log.info("📄 Rapport HTML généré : {}", htmlFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("❌ Erreur lors de la génération du rapport HTML : {}", e.getMessage());
        }
    }

    public DependencyChecker() {
        this.webClient = WebClient.builder()
                .baseUrl(NEXUS_URL)
                .defaultHeaders(headers -> headers.setBasicAuth(NEXUS_USERNAME, NEXUS_PASSWORD))
                .build();
    }

    public List<String> checkCVEWithNexus(String groupId, String artifactId, String version) {
        List<String> vulnerabilities = new ArrayList<>();

        try {
            Map<String, Object> component = Map.of(
                    "componentIdentifier", Map.of(
                            "format", "maven",
                            "coordinates", Map.of(
                                    "groupId", groupId,
                                    "artifactId", artifactId,
                                    "version", version
                            )
                    )
            );
            Map<String, Object> requestBody = Map.of("components", List.of(component));
            // Appel API Nexus
            Map response = webClient.post()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();  // Exécuter la requête de manière synchrone

            if (response != null && response.containsKey("components")) {
                List<Map<String, Object>> components = (List<Map<String, Object>>) response.get("components");
                for (Map<String, Object> comp : components) {
                    if (comp.containsKey("vulnerabilities")) {
                        List<Map<String, Object>> vulns = (List<Map<String, Object>>) comp.get("vulnerabilities");
                        for (Map<String, Object> vuln : vulns) {
                            String cve = (String) vuln.get("id");
                            String description = (String) vuln.get("description");
                            vulnerabilities.add(cve + " - " + description);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de la requête Nexus : {}", e.getMessage());
        }

        return vulnerabilities;
    }

    public boolean checkDependenciesWithNexus() {
        Map<String, String> dependencies = extractDependencies(new File("pom.xml"));

        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            String[] parts = entry.getKey().split(":");
            String groupId = parts[0];
            String artifactId = parts[1];
            String version = entry.getValue();

            List<String> cveList = checkCVEWithNexus(groupId, artifactId, version);
            if (!cveList.isEmpty()) {
                log.warn("⚠️ Vulnérabilités détectées pour {} version {} :\n{}", entry.getKey(), version, String.join("\n", cveList));
            }
        }
        return true;
    }
}
