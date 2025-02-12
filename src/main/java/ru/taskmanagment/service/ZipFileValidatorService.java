package ru.taskmanagment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ZipFileValidatorService {

    private static final List<String> REQUIRED_FILES = List.of("src/");
    private static final List<String> VALID_EXTENSIONS = List.of(".java", ".py", ".js", ".html", ".css", ".json", ".xml");
    private static final List<String> FILE_EXTENSIONS = List.of(".txt", ".md");
    private static final long MAX_ZIP_SIZE = 10 * 1024 * 1024; // 10 MB
    private final GitBranchService gitBranchService;

    /**
     * Validates a ZIP file and pushes it to the appropriate branch in the remote repository.
     *
     * @param file The uploaded ZIP file.
     * @return A list of validation errors, or an empty list if the file is valid.
     * @throws IOException If an I/O error occurs during processing.
     */
    @Transactional
    public List<String> validateZipFile(MultipartFile file, String currentBranch) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        if (file.getSize() > MAX_ZIP_SIZE) {
            throw new IllegalArgumentException("Uploaded file exceeds maximum size limit.");
        }

        // Create temporary directories
        Path tempDir = createTempDirectory("zip-extraction-");
        Path gitCloneDir = createTempDirectory("git-clone-");

        try {
            // Save the uploaded file to a temporary file
            Path tempFilePath = Files.createTempFile("uploaded-zip-", ".zip");
            try (OutputStream outputStream = Files.newOutputStream(tempFilePath)) {
                file.getInputStream().transferTo(outputStream);
            }

            // Validate the structure of the ZIP before extraction
            validateZipStructure(tempFilePath.toFile());

            // Extract the ZIP file
            extractZipFile(file, tempDir);

            // Optionally validate file integrity using hashes
            Map<String, String> expectedHashes = Map.of(
                    "src/main.java", "expected_sha256_hash_here" // Example hash for demonstration
            );
            validateFileIntegrity(tempDir, expectedHashes);

            // Detect the project root directory
            String projectRootDir = detectProjectRoot(tempDir);
            if (projectRootDir == null || projectRootDir.isEmpty()) {
                throw new IOException("No project root directory detected in the ZIP file.");
            }

            // Validate the extracted files
            List<String> validationErrors = validateProjectStructure(tempDir, projectRootDir);

            // Log the current branch
            log.info("Detected current branch: {}", currentBranch);

            // Push to the appropriate branch based on validation results
            String remoteUrl = "https://gitlab.com/backend2391702/testapplication.git";
            gitBranchService.pushToCorrectBranch(remoteUrl,
                    gitCloneDir,
                    tempDir,
                    projectRootDir,
                    validationErrors,
                    currentBranch);

            return validationErrors;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            // Clean up temporary directories
            cleanup(tempDir.toFile());
            cleanup(gitCloneDir.toFile());
        }
    }

    /**
     * Creates a temporary directory with the specified prefix.
     */
    private Path createTempDirectory(String prefix) throws IOException {
        Path tempDir = Files.createTempDirectory(prefix);
        log.info("Created temporary directory: {}", tempDir);
        return tempDir;
    }
    /**
     * Extracts the contents of a ZIP file into the specified directory.
     */
    private void extractZipFile(MultipartFile file, Path tempDir) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path extractedPath = tempDir.resolve(entry.getName()).normalize();
                if (entry.isDirectory()) {
                    Files.createDirectories(extractedPath);
                } else {
                    Files.createDirectories(extractedPath.getParent());
                    try (BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(extractedPath))) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        }
        log.info("Extracted ZIP file to: {}", tempDir);
    }

    /**
     * Validates the structure of the ZIP archive using Apache Commons Compress.
     */
    private void validateZipStructure(File zipFile) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
            Set<String> requiredFiles = Set.of("src/", "README.md"); // Exemple de fichiers/dossiers requis
            Set<String> foundFiles = new HashSet<>();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String entryName = entry.getName();

                log.info("Detected entry: {}", entryName);
                foundFiles.add(entryName);

                // Détecter des anomalies potentielles
                if (entryName.contains("..") || entryName.startsWith("/")) {
                    throw new IOException("Invalid entry name: " + entryName);
                }
            }

            // Vérifier les fichiers manquants
            requiredFiles.stream()
                    .filter(required -> !foundFiles.contains(required))
                    .forEach(missing -> log.warn("Missing required file/directory: {}", missing));
        }
    }

    private void validateFileTypes(Path tempDir) throws IOException {
        Tika tika = new Tika();
        Files.walk(tempDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        String detectedType = tika.detect(path);
                        String expectedType = getExpectedMimeType(path.toString());
                        if (!detectedType.equals(expectedType)) {
                            log.warn("Mismatched file type: {} (expected: {}, detected: {})",
                                    path, expectedType, detectedType);
                        }
                    } catch (IOException e) {
                        log.error("Failed to detect file type: {}", path, e);
                    }
                });
    }

    private void validateFileNames(Path tempDir) throws IOException {
        Files.walk(tempDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    if (!fileName.matches("[a-zA-Z0-9_\\-.]+")) {
                        log.warn("Invalid file name: {}", fileName);
                    }
                });
    }

    private String getExpectedMimeType(String fileName) {
        if (fileName.endsWith(".java")) return "text/x-java-source";
        if (fileName.endsWith(".json")) return "application/json";
        if (fileName.endsWith(".xml")) return "application/xml";
        if (fileName.endsWith(".txt")) return "text/plain";
        if (fileName.endsWith(".html")) return "text/html";
        if (fileName.endsWith(".css")) return "text/css";
        if (fileName.endsWith(".js")) return "application/javascript";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg")) return "image/jpeg";
        if (fileName.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }

    /**
     * Calculates the SHA-256 hash of a file.
     */
    private String calculateFileHash(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
    }

    /**
     * Validates the integrity of files using their hashes.
     */
    private void validateFileIntegrity(Path tempDir, Map<String, String> expectedHashes) throws IOException, NoSuchAlgorithmException {
        Files.walk(tempDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        String fileName = tempDir.relativize(path).toString();
                        String actualHash = calculateFileHash(path);
                        String expectedHash = expectedHashes.get(fileName);
                        if (expectedHash != null && !expectedHash.equals(actualHash)) {
                            throw new IOException("File hash mismatch: " + fileName);
                        }
                    } catch (IOException | NoSuchAlgorithmException e) {
                        log.error("Failed to validate file integrity: {}", path, e);
                        throw new RuntimeException(e);
                    }
                        }
                );
    }

    /**
     * Detects the root directory of the project within the extracted ZIP file.
     */
    private String detectProjectRoot(Path tempDir) throws IOException {
        return Files.walk(tempDir, 1)
                .filter(Files::isDirectory)
                .map(dir -> tempDir.relativize(dir).toString())
                .filter(name -> !name.isEmpty() && !name.equals("."))
                .findFirst()
                .orElse(null);
    }

    /**
     * Validates the structure of the extracted project.
     */
    private List<String> validateProjectStructure(Path tempDir, String projectRootDir) throws IOException {
        List<String> validationErrors = new ArrayList<>();
        Path sourceDir = tempDir.resolve(projectRootDir);

        if (!Files.exists(sourceDir)) {
            validationErrors.add("Source directory does not exist: " + sourceDir);
            return validationErrors;
        }

        // Check for required files/directories
        for (String requiredFile : REQUIRED_FILES) {
            Path requiredPath = sourceDir.resolve(requiredFile);
            if (!Files.exists(requiredPath)) {
                validationErrors.add("Missing required file or directory: " + requiredFile);
            }
        }

        // Check for valid files in the `src/` directory
        Path srcPath = sourceDir.resolve("src");
        if (!Files.isDirectory(srcPath)) {
            validationErrors.add("The `src/` directory is missing.");
        } else {
            boolean validSrcFiles = Files.walk(srcPath)
                    .filter(path -> !Files.isDirectory(path))
                    .anyMatch(path -> VALID_EXTENSIONS.stream().anyMatch(path.toString()::endsWith));
            if (!validSrcFiles) {
                validationErrors.add("The `src/` directory contains no valid files.");
            }
        }

        // Check for metadata files
        boolean containsMetadataFile = Files.walk(tempDir)
                .filter(path -> !Files.isDirectory(path))
                .anyMatch(path -> FILE_EXTENSIONS.stream().anyMatch(path.toString()::endsWith));
        if (!containsMetadataFile) {
            validationErrors.add("No `.txt`, `.md`, `.json`, or `.xml` file found in the ZIP archive.");
        }

        return validationErrors;
    }

    /**
     * Deletes all contents of a directory recursively.
     */
    private void cleanup(File directory) {
        if (directory.exists()) {
            Arrays.stream(Objects.requireNonNull(directory.listFiles())).forEach(file -> {
                if (file.isDirectory()) {
                    cleanup(file);
                }
                if (!file.delete()) {
                    log.warn("Failed to delete file: {}", file.getAbsolutePath());
                }
            });
        }
    }
}
