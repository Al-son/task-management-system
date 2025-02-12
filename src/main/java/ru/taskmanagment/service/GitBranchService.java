package ru.taskmanagment.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitBranchService {

    /**
     * Pushes the validated files to the correct branch in the remote repository.
     */
    @Transactional
    public void pushToCorrectBranch(String remoteUrl, Path gitCloneDir, Path tempDir, String projectRootDir,
                                    List<String> validationErrors, String currentBranch) {
        File localRepoDir = gitCloneDir.toFile();
        Git git = null;

        try {
            Path sourceDir = tempDir.resolve(projectRootDir);
            if (!Files.exists(sourceDir)) {
                log.error("Source directory does not exist: {}", sourceDir);
                return;
            }

            // Clone the repository with credentials
            log.info("Cloning repository into directory: {}", localRepoDir);
            git = Git.cloneRepository()
                    .setURI(remoteUrl)
                    .setDirectory(localRepoDir)
                    .setBranchesToClone(List.of("refs/heads/*"))  // ✅ Fetch all branches
                    .setCloneAllBranches(true)
                    .setCredentialsProvider(getCredentialsProvider()) // Set credentials
                    .call();

            // Ensure the remote "origin" is configured
            boolean hasOrigin = git.getRepository().getConfig().getSubsections("remote").contains("origin");
            if (!hasOrigin) {
                log.warn("Remote 'origin' is not configured. Adding remote 'origin'.");
                git.remoteAdd()
                        .setName("origin")
                        .setUri(new URIish(remoteUrl))
                        .call();
            }

            // Determine the branch to push to
            String branchToPush = validationErrors.isEmpty() ? getDefaultBranch(git) : currentBranch;
            log.info("Checking out branch: {}", branchToPush);

            // Checkout the branch, creating it if it doesn't exist
            git.checkout()
                    .setName(branchToPush)
                    .setCreateBranch(!branchExists(git, branchToPush))
                    .call();

            // Pull the latest changes from the remote branch with credentials
            pullLatestChanges(git, branchToPush);

            // Copy files to the cloned repository directory
            copyFilesToRepository(sourceDir, localRepoDir.toPath());

            // Add, commit, and push changes with credentials
            addAndCommitChanges(git, branchToPush);
            pushChanges(git, branchToPush);
        } catch (GitAPIException | IOException | URISyntaxException e) {
            log.error("Error pushing to branch: {}", e.getMessage(), e);
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }
    /**
     * Provides credentials securely.
     */
    private UsernamePasswordCredentialsProvider getCredentialsProvider() {
        String username = System.getenv("GIT_USERNAME");
        String password = System.getenv("GIT_PASSWORD");
        if (username == null || password == null) {
            log.error("Git credentials are missing. Username: {}, Password: {}", username, password);
            throw new IllegalStateException("Git credentials (username or password) are not configured.");
        }

        log.debug("Using Git credentials - Username: {}, Password: {}", username, "*****");
        return new UsernamePasswordCredentialsProvider(username, password);
    }

    /**
     * Determines the default branch of the repository (either "main" or "master").
     */
    private static String getDefaultBranch(Git git) throws GitAPIException {
        List<Ref> branches = git.branchList().call();
        if (branches.isEmpty()) {
            log.warn("No branches found. Defaulting to 'main'.");
            return "main";
        }

        Optional<Ref> mainBranch = branches.stream()
                .filter(ref -> ref.getName().endsWith("main"))
                .findFirst();
        Optional<Ref> masterBranch = branches.stream()
                .filter(ref -> ref.getName().endsWith("master"))
                .findFirst();

        return mainBranch.map(ref -> "main")
                .orElseGet(() -> masterBranch.map(ref -> "master")
                        .orElseGet(() -> branches.get(0).getName().substring("refs/heads/".length())));
    }

    /**
     * Checks if a branch exists in the repository.
     */
    private boolean branchExists(Git git, String branchName) throws GitAPIException {
        return git.branchList().call().stream()
                .anyMatch(ref -> ref.getName().endsWith(branchName));
    }

    /**
     * Detects the current branch of the repository.
     */
    public String getCurrentBranch(Git git) throws IOException, GitAPIException, URISyntaxException {
        Repository repository = git.getRepository();
        String branch = repository.getBranch();

        if (branch == null || branch.equals("HEAD")) {
            log.warn("Repository is in detached HEAD state. Attempting to resolve the current branch.");

            // Try to resolve the current branch from HEAD
            Ref headRef = repository.exactRef("HEAD");
            if (headRef != null) {
                Ref targetRef = headRef.isSymbolic() ? headRef.getTarget() : headRef;
                if (targetRef != null) {
                    branch = targetRef.getName();
                    if (branch.startsWith("refs/heads/")) {
                        branch = branch.substring("refs/heads/".length());
                        log.info("Resolved current branch: {}", branch);
                        return branch;
                    }
                }
            }

            log.warn("Unable to resolve current branch. Fetching branches...");

            // Ensure the remote "origin" is configured
            boolean hasOrigin = repository.getConfig().getSubsections("remote").contains("origin");
            if (!hasOrigin) {
                log.warn("Remote 'origin' is not configured. Adding remote 'origin'.");
                git.remoteAdd()
                        .setName("origin")
                        .setUri(new URIish("https://gitlab.com/backend2391702/testapplication.git"))
                        .call();
            }

            // Fetch branches from the remote with credentials
            git.fetch()
                    .setRemote("origin")
                    .setCredentialsProvider(getCredentialsProvider()) // Set credentials
                    .call();

            // Try listing branches if HEAD resolution fails
            List<Ref> branches = git.branchList().call();
            for (Ref ref : branches) {
                if (ref.getName().startsWith("refs/heads/")) {
                    String branchName = ref.getName().substring("refs/heads/".length());
                    log.info("Found branch: {}", branchName);
                    return branchName; // Return the first valid branch
                }
            }

            log.warn("No branches found. Defaulting to 'main'.");
            return "main"; // Fallback to main
        }

        return branch;
    }

    public static void ensureBranch(Git git, String expectedBranch) throws GitAPIException, IOException {
        String currentBranch = git.getRepository().getBranch();
        if (!currentBranch.equals(expectedBranch)) {
            log.info("Switching to expected branch: {}", expectedBranch);
            git.checkout().setName(expectedBranch).call();
        }
    }

    /**
     * Pulls the latest changes from the remote branch.
     */
    private void pullLatestChanges(Git git, String branchToPush) {
        try {
            git.pull()
                    .setRemote("origin")
                    .setRemoteBranchName(branchToPush)
                    .setCredentialsProvider(getCredentialsProvider())
                    .call();
        } catch (GitAPIException e) {
            log.warn("Failed to pull from remote branch: {}. This may be expected if the branch is new.", branchToPush);
        }
    }

    /**
     * Copies files from the source directory to the destination directory.
     */
    private void copyFilesToRepository(Path sourceDir, Path destinationDir) throws IOException {
        Files.walk(sourceDir).forEach(source -> {
            try {
                Path destination = destinationDir.resolve(sourceDir.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy files: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Adds and commits changes to the repository.
     */
    private void addAndCommitChanges(Git git, String branchToPush) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit()
                .setMessage("Commit to branch " + branchToPush)
                .call();
    }

    /**
     * Pushes changes to the specified branch in the remote repository.
     *
     * @param git         The Git instance representing the cloned repository.
     * @param branchToPush The name of the branch to push changes to.
     * @throws GitAPIException If an error occurs during the Git push operation.
     */
    private void pushChanges(Git git, String branchToPush) throws GitAPIException {
        try {
            log.info("Pushing changes to remote branch: {}", branchToPush);

            // Use secure credentials (e.g., from environment variables)
            UsernamePasswordCredentialsProvider credentialsProvider = getCredentialsProvider();

            // Perform the push operation
            Iterable<PushResult> pushResults = git.push()
                    .setRemote("origin") // Remote repository name (usually "origin")
                    .setCredentialsProvider(credentialsProvider)
                    .call();

            // Log the results of the push operation
            pushResults.forEach(pushResult -> {
                log.info("Push result: {}", pushResult.getMessages());
                pushResult.getRemoteUpdates().forEach(update -> {
                    log.info("Remote update: {}", update);
                });
            });

        } catch (GitAPIException e) {
            log.error("Error pushing changes to branch {}: {}", branchToPush, e.getMessage(), e);
            throw e; // Rethrow the exception to propagate the error
        }
    }
}
