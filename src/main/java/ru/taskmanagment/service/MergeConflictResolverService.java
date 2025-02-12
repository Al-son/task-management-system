package ru.taskmanagment.service;

import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.taskmanagment.entity.ConflictResolution;
import ru.taskmanagment.repository.ConflictResolutionRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MergeConflictResolverService {

    private static final Logger logger = LoggerFactory.getLogger(MergeConflictResolverService.class);
    private final ConflictResolutionRepository conflictResolutionRepository;

    public String resolveConflicts(String repoPath, String strategy) {
        Objects.requireNonNull(repoPath, "Repository path must not be null");
        Objects.requireNonNull(strategy, "Strategy must not be null");

        File repoDir = new File(repoPath, ".git");
        if (!repoDir.exists()) {
            return "❌ Repository does not exist at the specified path.";
        }

        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(repoDir)
                .readEnvironment()
                .findGitDir()
                .build()) {

            try (Git git = new Git(repository)) {
                if (!new File(repoDir, "MERGE_HEAD").exists()) {
                    return "ℹ️ No merge in progress. Please perform a merge before checking for conflicts.";
                }

                List<String> conflictingFiles = getConflictingFiles(git);
                if (conflictingFiles.isEmpty()) {
                    return "✅ No conflicts detected.";
                }

                String conflictDetails = getConflictDetails(git, conflictingFiles);
                resolveConflictsAutomatically(git, conflictingFiles, strategy);

                // ✅ NEW: Commit only once after all conflicts are resolved
                git.commit().setMessage("Automatic conflict resolution (" + strategy + ")").call();

                return "⚠️ Conflicting files:\n" + conflictDetails;
            }
        } catch (Exception e) {
            logger.error("Error during conflict detection and resolution", e);
            return "❌ Error during conflict detection: " + e.getMessage();
        }
    }

    public String detectConflicts(String repoPath) {
        Objects.requireNonNull(repoPath, "Repository path must not be null");

        File repoDir = new File(repoPath, ".git");
        if (!repoDir.exists()) {
            return "❌ Repository does not exist at the specified path.";
        }

        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(repoDir)
                .readEnvironment()
                .findGitDir()
                .build()) {

            try (Git git = new Git(repository)) {
                if (!new File(repoDir, "MERGE_HEAD").exists()) {
                    return "ℹ️ No merge in progress. Please perform a merge before checking for conflicts.";
                }

                List<String> conflictingFiles = getConflictingFiles(git);
                if (conflictingFiles.isEmpty()) {
                    return "✅ No conflicts detected.";
                }

                return "⚠️ Conflicting files:\n" + getConflictDetails(git, conflictingFiles);
            }
        } catch (Exception e) {
            logger.error("Error during conflict detection", e);
            return "❌ Error during conflict detection: " + e.getMessage();
        }
    }

    public String abortMerge(String repoPath) {
        Objects.requireNonNull(repoPath, "Repository path must not be null");

        File repoDir = new File(repoPath, ".git");
        if (!repoDir.exists()) {
            return "❌ Repository does not exist at the specified path.";
        }

        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(repoDir)
                .readEnvironment()
                .findGitDir()
                .build()) {

            try (Git git = new Git(repository)) {
                File mergeHeadFile = new File(repoDir, "MERGE_HEAD");

                if (mergeHeadFile.exists()) {
                    // Abort the merge if it's still in progress
                    git.reset().setMode(ResetCommand.ResetType.HARD).call();
                    return "✅ Merge aborted successfully.";
                }

                // If no merge is in progress, check if the last commit was an automatic resolution
                String lastCommitMessage = git.log().setMaxCount(1).call().iterator().next().getFullMessage();

                if (lastCommitMessage.startsWith("Automatic conflict resolution")) {
                    // Undo the last commit only if it was an automatic conflict resolution
                    git.reset().setMode(ResetCommand.ResetType.HARD).setRef("HEAD~1").call();
                    return "✅ Automatic merge commit undone successfully.";
                } else {
                    return "ℹ️ Merge was already completed and committed. Cannot abort.";
                }
            }
        } catch (Exception e) {
            logger.error("Error during merge abort", e);
            return "❌ Error during merge abort: " + e.getMessage();
        }
    }

    private List<String> getConflictingFiles(Git git) throws GitAPIException {
        return git.status().call().getConflicting().stream().toList();
    }

    private String getConflictDetails(Git git, List<String> conflictingFiles) throws IOException, GitAPIException {
        StringBuilder details = new StringBuilder();
        for (String file : conflictingFiles) {
            details.append("- ").append(file).append("\n");
            details.append(getFileDiff(git.getRepository(), file)).append("\n");
        }
        return details.toString();
    }

    private String getFileDiff(Repository repository, String filePath) throws IOException, GitAPIException {
        try (Git git = new Git(repository);
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             DiffFormatter diffFormatter = new DiffFormatter(out)) {

            diffFormatter.setRepository(repository);
            AbstractTreeIterator oldTree = prepareTreeParser(repository, "HEAD");
            AbstractTreeIterator newTree = prepareTreeParser(repository, "MERGE_HEAD");

            List<DiffEntry> diffs = git.diff().setOldTree(oldTree).setNewTree(newTree).call();
            diffFormatter.format(diffs);
            return out.toString();
        }
    }

    private void resolveConflictsAutomatically(Git git,
                                               List<String> conflictingFiles,
                                               String strategy) throws IOException,
            GitAPIException {
        for (String filePath : conflictingFiles) {
            File file = new File(git.getRepository().getDirectory().getParent(), filePath);
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()));

            StringBuilder resolvedContent = new StringBuilder();
            boolean insideConflict = false;
            boolean inMineSection = false;
            boolean inTheirsSection = false;
            boolean keepMine = "keep-mine".equals(strategy);
            boolean keepTheirs = "keep-theirs".equals(strategy);

            for (String line : lines) {
                if (line.startsWith("<<<<<<<")) {
                    insideConflict = true;
                    inMineSection = true;
                    continue;
                } else if (line.startsWith("=======")) {
                    inMineSection = false;
                    inTheirsSection = true;
                    continue;
                } else if (line.startsWith(">>>>>>>")) {
                    insideConflict = false;
                    inTheirsSection = false;
                    continue;
                }

                if (!insideConflict) {
                    resolvedContent.append(line).append("\n");
                } else {
                    if (keepMine && inMineSection) {
                        resolvedContent.append(line).append("\n");
                    } else if (keepTheirs && inTheirsSection) {
                        resolvedContent.append(line).append("\n");
                    } else if (!keepMine && !keepTheirs) { // "merge" strategy
                        resolvedContent.append(line).append("\n");
                    }
                }
            }

            String diffBefore = getFileDiff(git.getRepository(), filePath);
            Files.write(Paths.get(file.getAbsolutePath()), resolvedContent.toString().getBytes());
            git.add().addFilepattern(filePath).call();
            String diffAfter = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            conflictResolutionRepository.save(new ConflictResolution(filePath, strategy, diffBefore, diffAfter));
        }

        git.commit().setMessage("Automatic conflict resolution (" + strategy + ")").call();
    }

    private AbstractTreeIterator prepareTreeParser(Repository repository, String ref) throws IOException, GitAPIException {
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        try (Git git = new Git(repository)) {
            treeParser.reset(repository.newObjectReader(), git.getRepository().resolve(ref + "^{tree}"));
        }
        return treeParser;
    }
}
