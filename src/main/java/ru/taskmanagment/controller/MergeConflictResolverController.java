package ru.taskmanagment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.taskmanagment.service.MergeConflictResolverService;
import ru.taskmanagment.service.MergeTestService;

@RestController
@RequestMapping("/merge")
@RequiredArgsConstructor
public class MergeConflictResolverController {

    private final MergeConflictResolverService mergeConflictResolverService;
    private final MergeTestService mergeTestService;


    @GetMapping("/conflicts")
    public ResponseEntity<String> detectConflicts(@RequestParam String repoPath) {
        String result = mergeConflictResolverService.detectConflicts(repoPath);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/resolve")
    public ResponseEntity<String> resolveConflicts(
            @RequestParam String repoPath,
            @RequestParam String strategy) {
        String result = mergeConflictResolverService.resolveConflicts(repoPath, strategy);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/abort")
    public ResponseEntity<String> abortMerge(@RequestParam String repoPath) {
        String result = mergeConflictResolverService.abortMerge(repoPath);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/test")
    public ResponseEntity<String> runTests(@RequestParam String repoPath, @RequestParam String strategy) {
        String result = mergeTestService.runTestsAfterMerge(repoPath, strategy);
        return ResponseEntity.ok(result);
    }
}