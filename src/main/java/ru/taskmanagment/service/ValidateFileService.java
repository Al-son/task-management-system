package ru.taskmanagment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.taskmanagment.service.test.DependencyChecker;
import ru.taskmanagment.service.test.SensitiveDataChecker;
import ru.taskmanagment.service.test.StaticCodeAnalyzer;
import ru.taskmanagment.service.test.TestRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Service
@Slf4j
@RequiredArgsConstructor
public class ValidateFileService {
    private static final List<String> REQUIRED_FILES = List.of("README.md", "src/", "requirements.txt");
    private static final List<String> VALID_EXTENSIONS = List.of(".py", ".js", ".java", ".html", ".css", ".kt");
    private static final List<String> FILE_EXTENSIONS = List.of(".txt", ".md", ".json", ".xml");
    private static final long MAX_ZIP_SIZE = 10 * 1024 * 1024; // 10 MB
    private final NewBranchService newBranchService;
    private final DependencyChecker dependencyChecker;
    private final StaticCodeAnalyzer staticCodeAnalyzer;
    private final SensitiveDataChecker sensitiveDataChecker;
    private final TestRunner testRunner;


    /**
     * Validates and unzips a ZIP file.
     */
    public void validateZipFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        if (file.getSize() > MAX_ZIP_SIZE) {
            throw new IllegalArgumentException("Uploaded file exceeds maximum size limit.");
        }
        Path tempDir = createTempDirectory();
        try {
            Path tempFilePath = saveFileToTemp(file);
            validateZipStructure(tempFilePath.toFile());
            extractZipFile(file, tempDir);
            validateFileIntegrity(tempDir, new HashMap<>());
            List<String> validationErrors = validateProjectStructure(tempDir);
            if (!validationErrors.isEmpty()) {
                throw new IllegalStateException("Zip file structure validation failed: " + validationErrors);
            }
            //log.info("Validated file !");
            //newBranchService.createBranchAndPush(tempDir);

            //Test checker
            if (!dependencyChecker.checkDependencies()) {
                throw new IllegalStateException("Dependency check failed. ");
            }
            if (!staticCodeAnalyzer.analyzeCode(tempFilePath)) {
                throw new IllegalStateException("Static code analysis failed.");
            }
            if (!sensitiveDataChecker.checkSensitiveData(tempFilePath)) {
                throw new IllegalStateException("Sensitive data check failed.");
            }
            if (!testRunner.runTests(tempDir)) {
                throw new IllegalStateException("Tests failed or coverage verification failed.");
            }
            log.info("File validated and processed successfully");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } finally {
            cleanup(tempDir.toFile());
        }
    }

    /**
     * Saves the uploaded file to a temporary location.
     */
    private Path saveFileToTemp(MultipartFile file) throws IOException {
        Path tempFilePath = Files.createTempFile("uploaded-zip-", ".zip");
        try (OutputStream outputStream = Files.newOutputStream(tempFilePath)) {
            file.getInputStream().transferTo(outputStream);
        }
        return tempFilePath;
    }

    /**
     * Validates the structure of the ZIP archive using Apache Commons Compress.
     */
    private void validateZipStructure(File zipFile) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Set<String> foundFiles = new HashSet<>();
            Enumeration<? extends ZipArchiveEntry> entries = zip.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                foundFiles.add(entry.getName());
                log.info("Detected entry: {}", entry.getName());

                // Check for invalid entry names
                if (entry.getName().contains("..") || entry.getName().startsWith("/")) {
                    throw new IOException("Invalid entry name: " + entry.getName());
                }
            }

            // Detect the project root directory
            Optional<String> rootDir = foundFiles.stream()
                    .filter(f -> f.endsWith("README.md") || f.endsWith("requirements.txt"))
                    .map(f -> f.substring(0, f.lastIndexOf('/') + 1)) // Extract parent directory
                    .findFirst();

            if (rootDir.isPresent()) {
                String projectRoot = rootDir.get();
                log.info("Detected project root: {}", projectRoot);

                // Validate required files relative to the project root
                for (String required : REQUIRED_FILES) {
                    boolean found = foundFiles.stream()
                            .anyMatch(f -> f.startsWith(projectRoot) && f.endsWith(required));
                    if (!found) {
                        throw new IOException("Missing required file or directory: " + required);
                    }
                }
            } else {
                throw new IOException("Could not detect project root in ZIP file.");
            }
        }
    }

    /**
     * Validates the project structure, ensuring required files and directories are present.
     */
    private List<String> validateProjectStructure(Path tempDir) throws IOException {
        List<String> validationErrors = new ArrayList<>();

        // Detect the project root dynamically
        Optional<Path> rootDir = Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals("README.md") || path.getFileName().toString().equals("requirements.txt"))
                .map(Path::getParent)
                .findFirst();

        if (!rootDir.isPresent()) {
            validationErrors.add("Could not detect project root in extracted files.");
            return validationErrors;
        }

        Path projectRoot = rootDir.get();

        // Validate required files relative to the project root
        for (String requiredFile : REQUIRED_FILES) {
            Path requiredPath = projectRoot.resolve(requiredFile);
            if (!Files.exists(requiredPath)) {
                validationErrors.add("Missing required file or directory: " + requiredFile + ". Expected location: " + requiredPath);
            }
        }
        // Validate the `src/` directory
        Path srcPath = projectRoot.resolve("src/");
        if (!Files.exists(srcPath)) {
            validationErrors.add("The `src/` directory is missing. Expected location: " + srcPath);
        } else {
            boolean validSrcFiles = Files.walk(srcPath) // Traverse the entire directory tree
                    .filter(path -> !Files.isDirectory(path))
                    .anyMatch(path -> VALID_EXTENSIONS.stream().anyMatch(path.toString()::endsWith));
            if (!validSrcFiles) {
                validationErrors.add("The `src/` directory contains no valid source files. Expected files with extensions: " + VALID_EXTENSIONS);
            }
        }

        // Validate metadata files
        boolean containsMetadataFile = Files.walk(projectRoot)
                .filter(path -> !Files.isDirectory(path))
                .anyMatch(path -> FILE_EXTENSIONS.stream().anyMatch(path.toString()::endsWith));
        if (!containsMetadataFile) {
            validationErrors.add("No metadata files (.txt, .md, .json, .xml) found in the project root.");
        }

        return validationErrors;
    }

    /**
     * Validates the integrity of the files by comparing the calculated SHA-256 hash with an expected hash.
     */
    private void validateFileIntegrity(Path tempDir, Map<String, String> expectedHashes) throws IOException, NoSuchAlgorithmException {
        Files.walk(tempDir)
                .filter(path -> !Files.isDirectory(path)) // Skip directories
                .forEach(path -> {
                    try {
                        String fileName = tempDir.relativize(path).toString();
                        String actualHash = calculateFileHash(path);  // Calculate the file's hash
                        String expectedHash = expectedHashes.get(fileName);  // Get the expected hash for the file

                        if (expectedHash != null) {
                            // Compare the calculated hash with the expected hash
                            if (!expectedHash.equals(actualHash)) {
                                log.error("File hash mismatch for {}. Expected: {}, but got: {}", fileName, expectedHash, actualHash);
                            }
                        }
                    } catch (IOException | NoSuchAlgorithmException e) {
                        log.error("Failed to validate file integrity for file: {}", path, e);
                    }
                });
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
     * Creates a temporary directory with the specified prefix.
     */
    private Path createTempDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("zip-extraction-");
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
