package ru.taskmanagment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.taskmanagment.service.ValidateFileService;

import java.io.IOException;

@RestController
@RequestMapping("/zip")
@RequiredArgsConstructor
public class ValidateFileController {
    private final ValidateFileService validateFileService;

    /**
     * Endpoint for uploading and validating a ZIP file.
     *
     * @param file the ZIP file to be uploaded
     * @return a response indicating the result of the validation
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadZipFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate and process the uploaded ZIP file
            validateFileService.validateZipFile(file);
            return new ResponseEntity<>("File uploaded and processed successfully.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException | IOException e) {
            return new ResponseEntity<>("An error occurred while processing the file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
