package com.tcpip147.tomcatconnector.program;

import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TomcatProgram {

    private final List<String> existsLibraries = new ArrayList<>();

    public void execute(@NotNull ExecutionEnvironment environment, Runnable runnable) {
        ProgressManager.getInstance().run(new Task.Backgroundable(environment.getProject(), "Starting...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                TomcatConfiguration configuration = (TomcatConfiguration) Objects.requireNonNull(environment.getRunnerAndConfigurationSettings()).getConfiguration();
                existsLibraries.clear();
                copyDeploymentAssembly(environment.getProject(), configuration);
                copyModuleLibraries(configuration);
                deleteLibrariesInDeploymentAssembly(environment.getProject(), configuration);
                ApplicationManager.getApplication().invokeLater(runnable);
            }
        });
    }

    private void copyDeploymentAssembly(@NotNull Project project, @NotNull TomcatConfiguration configuration) {
        String basePath = project.getBasePath();
        Map<String, String> deploymentAssembly = configuration.getOptions().getDeploymentAssembly();
        Path ptDocBase = Paths.get(configuration.getOptions().getDocBase());
        Path ptLib = ptDocBase.resolve("WEB-INF").resolve("lib");
        for (String key : deploymentAssembly.keySet()) {
            Path ptSource = Paths.get(basePath + key);
            Path ptTarget = Paths.get(ptDocBase + deploymentAssembly.get(key));

            FileUtil.processFilesRecursively(ptSource.toFile(), sourceFile -> {
                if (sourceFile.isFile() && !FileUtil.extensionEquals(sourceFile.getPath(), "java")) {
                    File targetFile = new File(ptTarget + sourceFile.getPath().substring(ptSource.toString().length()));
                    if (targetFile.getPath().startsWith(ptLib.toString())) {
                        existsLibraries.add(targetFile.getPath());
                    }
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

            FileUtil.processFilesRecursively(ptTarget.toFile(), targetFile -> {
                Path ptRelative = Paths.get(targetFile.getPath().substring(ptTarget.toString().length()));
                File sourceFile = new File(ptSource + ptRelative.toString());
                if (!sourceFile.exists()) {
                    if (!targetFile.getPath().startsWith(ptLib.toString())) {
                        FileUtil.delete(targetFile);
                    }
                }
                return true;
            });
        }
    }

    private void copyModuleLibraries(TomcatConfiguration configuration) {
        Path ptDocBase = Paths.get(configuration.getOptions().getDocBase());
        Path ptLib = ptDocBase.resolve("WEB-INF").resolve("lib");
        Module module = configuration.getConfigurationModule().getModule();
        ModuleRootManager.getInstance(Objects.requireNonNull(module)).orderEntries().forEach(orderEntry -> {
            if (orderEntry instanceof ExportableOrderEntry exportableOrderEntry) {
                if (exportableOrderEntry.getScope() == DependencyScope.COMPILE || exportableOrderEntry.getScope() == DependencyScope.RUNTIME) {
                    if (orderEntry instanceof LibraryOrderEntry libraryOrderEntry) {
                        VirtualFile[] jars = libraryOrderEntry.getRootFiles(OrderRootType.CLASSES);
                        for (VirtualFile jar : jars) {
                            try {
                                File sourceFile = new File(jar.getPath().substring(0, jar.getPath().length() - 2));
                                File targetFile = ptLib.resolve(jar.getName()).toFile();
                                existsLibraries.add(targetFile.getPath());
                                if (!targetFile.exists() || sourceFile.lastModified() > targetFile.lastModified()) {
                                    FileUtil.copy(sourceFile, targetFile);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
            return true;
        });

        if (ptLib.toFile().listFiles() != null) {
            for (File file : Objects.requireNonNull(ptLib.toFile().listFiles())) {
                if (!existsLibraries.contains(file.getPath())) {
                    FileUtil.delete(file);
                }
            }
        }
    }

    private void deleteLibrariesInDeploymentAssembly(@NotNull Project project, TomcatConfiguration configuration) {
        String basePath = project.getBasePath();
        Map<String, String> deploymentAssembly = configuration.getOptions().getDeploymentAssembly();
        Path ptDocBase = Paths.get(configuration.getOptions().getDocBase());
        Path ptLib = ptDocBase.resolve("WEB-INF").resolve("lib");
        for (String key : deploymentAssembly.keySet()) {
            Path ptSource = Paths.get(basePath + key);
            Path ptTarget = Paths.get(ptDocBase + deploymentAssembly.get(key));

            FileUtil.processFilesRecursively(ptTarget.toFile(), targetFile -> {
                Path ptRelative = Paths.get(targetFile.getPath().substring(ptTarget.toString().length()));
                File sourceFile = new File(ptSource + ptRelative.toString());
                if (!sourceFile.exists()) {
                    if (targetFile.getPath().startsWith(ptLib.toString()) && !existsLibraries.contains(targetFile.getPath())) {
                        FileUtil.delete(targetFile);
                    }
                }
                return true;
            });
        }
    }
}
