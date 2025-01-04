package com.tcpip147.tomcatconnector.program;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class TomcatProgram {

    public void execute(@NotNull ExecutionEnvironment environment, Runnable runnable) throws ExecutionException {
        ProgressManager.getInstance().run(new Task.Backgroundable(environment.getProject(), "Starting...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                TomcatConfiguration configuration = (TomcatConfiguration) Objects.requireNonNull(environment.getRunnerAndConfigurationSettings()).getConfiguration();
                copyDeploymentAssembly(environment.getProject(), configuration);
                ApplicationManager.getApplication().invokeLater(runnable);
            }
        });
    }

    private void copyDeploymentAssembly(@NotNull Project project, @NotNull TomcatConfiguration configuration) {
        String basePath = project.getBasePath();
        Map<String, String> deploymentAssembly = configuration.getOptions().getDeploymentAssembly();
        Path ptDocBase = Paths.get(configuration.getOptions().getCatalinaBase());
        for (String key : deploymentAssembly.keySet()) {
            Path ptSource = Paths.get(basePath + key);
            Path ptTarget = Paths.get(ptDocBase + deploymentAssembly.get(key));
            FileUtil.processFilesRecursively(ptSource.toFile(), sourceFile -> {
                if (sourceFile.isFile() && !FileUtil.extensionEquals(sourceFile.getPath(), "java")) {
                    File targetFile = new File(ptTarget + sourceFile.getPath().substring(ptSource.toString().length()));
                    if (!targetFile.exists() || sourceFile.lastModified() > targetFile.lastModified()) {
                        try {
                            FileUtil.copy(sourceFile, targetFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return true;
            });
        }
    }
}
