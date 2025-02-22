package ru.taskmanagment.config.security;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class GitConfig {
    @Bean
    public Git git() throws IOException {
        File repoDir = new File("https://gitlab.com/backend2391702/testapplication");
        return new Git(new FileRepositoryBuilder().setGitDir(repoDir).readEnvironment().findGitDir().build());
    }
}
