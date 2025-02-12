package ru.taskmanagment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.taskmanagment.service.ZipFileValidatorService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/validate")
@RequiredArgsConstructor
public class ZipFileValidatorController {
    private final ZipFileValidatorService zipFileValidatorService;

    @PostMapping
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("branch") String branch) {
        try {
            List<String> validationErrors = zipFileValidatorService.validateZipFile(file, branch);
            if (validationErrors.isEmpty()) {
                return ResponseEntity.ok("File uploaded and validated successfully.");
            } else {
                return ResponseEntity.badRequest().body(validationErrors);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the file: " + e.getMessage());
        }
    }
}
