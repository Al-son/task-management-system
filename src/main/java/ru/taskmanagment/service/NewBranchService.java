package ru.taskmanagment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewBranchService {

    /**
     * Creates a new branch, adds the files to the branch, commits the changes, and pushes to the remote repository.
     */
    public void createBranchAndPush(Path tempDir) throws IOException, GitAPIException {
        // Path to the local Git repository
        String repositoryPath = "/path/to/your/git/repository";
        File repoDir = new File(repositoryPath);

        // Open the existing repository
        Git git = Git.open(repoDir);
        Repository repository = git.getRepository();

        // Generate a new branch name
        String newBranch = "feature/new-branch-" + UUID.randomUUID();

        // Create and checkout the new branch
        git.checkout().setCreateBranch(true).setName(newBranch).call();

        // Copy the extracted files from the temporary directory to the local Git repository
        Files.walk(tempDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        Path relativePath = tempDir.relativize(path);
                        Path destinationPath = repoDir.toPath().resolve(relativePath);
                        Files.createDirectories(destinationPath.getParent());
                        Files.copy(path, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        log.error("Error copying file to Git repository: {}", path, e);
                    }
                });

        // Stage and commit the changes
        git.add().addFilepattern(".").call();
        git.commit().setMessage("Add project files from ZIP").call();

        // Push the new branch to the remote repository
        // Your remote repository URL
        String remoteRepository = "https://github.com/your/repo.git";
        git.push().setRemote(remoteRepository).setRefSpecs(new RefSpec("refs/heads/" + newBranch + ":refs/heads/" + newBranch)).call();

        log.info("Created new branch: {} and pushed changes.", newBranch);
    }
}
