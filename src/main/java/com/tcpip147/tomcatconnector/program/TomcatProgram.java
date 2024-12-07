package com.tcpip147.tomcatconnector.program;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TomcatProgram {

    public void execute(@NotNull ExecutionEnvironment environment, Runnable runnable) throws ExecutionException {
        ProgressManager.getInstance().run(new Task.Backgroundable(environment.getProject(), "Starting...", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                TomcatConfiguration configuration = (TomcatConfiguration) environment.getRunnerAndConfigurationSettings().getConfiguration();
                Path ptCatalinaHome = Paths.get(configuration.getOptions().getCatalinaHome());
                Path ptCatalinaBase = Paths.get(configuration.getOptions().getCatalinaBase());
                Path ptDocBase = Paths.get(configuration.getOptions().getDocBase());
                Path ptClasses = ptDocBase.resolve("WEB-INF").resolve("classes");
                Path ptLib = ptDocBase.resolve("WEB-INF").resolve("lib");
                Path ptJavac = Paths.get(ProjectRootManager.getInstance(environment.getProject()).getProjectSdk().getHomePath()).resolve("bin").resolve("javac.exe");

                List<Path[]> assemblyFolders = getAssemblyFolders(configuration);
                for (Path[] assemblyFolder : assemblyFolders) {
                    FileUtil.processFilesRecursively(assemblyFolder[0].toFile(), file -> {
                        Path ptRelative = Paths.get(file.getPath().toString().substring(assemblyFolder[0].toString().length()));
                        Path ptTarget = Paths.get(assemblyFolder[1] + ptRelative.toString());
                        if (!ptTarget.toFile().exists() || (file.isFile() && file.lastModified() > ptTarget.toFile().lastModified())) {
                            try {
                                FileUtil.copyFileOrDir(file, ptTarget.toFile());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return true;
                    });

                    FileUtil.processFilesRecursively(assemblyFolder[1].toFile(), file -> {
                        Path ptRelative = Paths.get(file.getPath().toString().substring(assemblyFolder[1].toString().length()));
                        Path ptSource = Paths.get(assemblyFolder[0] + ptRelative.toString());
                        if (!ptSource.toFile().exists() && !file.getPath().startsWith(ptLib.toString()) && !file.getPath().startsWith(ptClasses.toString())) {
                            FileUtil.delete(file);
                        }
                        return true;
                    });
                }

                List<String> existsLibraries = new ArrayList<>();
                ModuleRootManager.getInstance(configuration.getConfigurationModule().getModule()).orderEntries().forEachLibrary(library -> {
                    VirtualFile[] jars = library.getFiles(OrderRootType.CLASSES);
                    for (VirtualFile jar : jars) {
                        try {
                            File source = new File(jar.getPath().substring(0, jar.getPath().length() - 2));
                            File target = ptLib.resolve(jar.getName()).toFile();
                            existsLibraries.add(target.getPath());
                            if (!target.exists() || source.lastModified() > target.lastModified()) {
                                FileUtil.copy(source, target);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return true;
                });

                if (ptLib.toFile().listFiles() != null) {
                    for (File file : ptLib.toFile().listFiles()) {
                        if (!existsLibraries.contains(file.getPath())) {
                            FileUtil.delete(file);
                        }
                    }
                }

                FileUtil.delete(ptClasses.toFile());

                List<String> existsClasses = new ArrayList<>();
                List<String> haveToCompiles = new ArrayList<>();
                VirtualFile[] sourceRoots = ModuleRootManager.getInstance(configuration.getConfigurationModule().getModule()).orderEntries().getSourceRoots();
                for (VirtualFile sourceRoot : sourceRoots) {
                    if (sourceRoot.getPath().startsWith(configuration.getConfigurationModule().getModule().getProject().getBasePath())) {
                        File sourceRootFile = new File(sourceRoot.getPath());
                        FileUtil.processFilesRecursively(sourceRootFile, file -> {
                            if (file.getPath().startsWith(sourceRootFile.getPath())) {
                                if (file.isFile()) {
                                    Path ptRelative = Paths.get(file.getPath().toString().substring(sourceRootFile.getPath().length()));
                                    Path ptTarget = Paths.get(ptClasses + ptRelative.toString());
                                    existsClasses.add(ptTarget.toString());
                                    try {
                                        if (FileUtil.extensionEquals(ptTarget.toString(), "java")) {
                                            haveToCompiles.add(file.toString());
                                        } else {
                                            if (!ptTarget.toFile().exists() || file.lastModified() > ptTarget.toFile().lastModified()) {
                                                FileUtil.copy(file, ptTarget.toFile());
                                            }

                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            return true;
                        });
                    }
                }

                FileUtil.processFilesRecursively(ptClasses.toFile(), file -> {
                    if (file.isFile() && !existsClasses.contains(file.getPath())) {
                        FileUtil.delete(file);
                    }
                    return true;
                });

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(ptCatalinaBase.resolve("sources.txt").toFile()))) {
                    for (String javaFile : haveToCompiles) {
                        bw.write(javaFile);
                        bw.newLine();
                    }
                    bw.flush();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (haveToCompiles.size() > 0) {
                    ProcessBuilder pb = new ProcessBuilder();
                    pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    List<String> commands = new ArrayList<>();
                    commands.add(ptJavac.toString());
                    commands.add("-d");
                    commands.add(ptClasses.toString());
                    commands.add("-classpath");

                    StringBuilder classpath = new StringBuilder();
                    classpath.append(ptLib.toAbsolutePath() + "\\*;");
                    commands.add(classpath.toString());

                    commands.add("-encoding");
                    commands.add("utf-8");
                    commands.add("@" + ptCatalinaBase.resolve("sources.txt"));

                    pb.command(commands);

                    try {
                        pb.start();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                ApplicationManager.getApplication().invokeLater(runnable);
            }
        });
    }

    private List<Path[]> getAssemblyFolders(TomcatConfiguration configuration) {
        List<Path[]> folders = new ArrayList<>();
        Module module = configuration.getConfigurationModule().getModule();
        String basePath = module.getProject().getBasePath();
        Path ptDocBase = Paths.get(configuration.getOptions().getDocBase());
        Map<String, String> assemblyMap = configuration.getOptions().getDeploymentAssembly();
        for (String key : assemblyMap.keySet()) {
            Path ptSource = Paths.get(basePath + key);
            Path ptTarget = Paths.get(ptDocBase + assemblyMap.get(key));
            Path[] path = new Path[2];
            path[0] = ptSource;
            path[1] = ptTarget;
            folders.add(path);
        }
        return folders;
    }

    private String getTargetFile(TomcatConfiguration configuration, String changedFile) {
        Module module = configuration.getConfigurationModule().getModule();
        String basePath = module.getProject().getBasePath();
        Path ptDocBase = Paths.get(configuration.getOptions().getDocBase());
        Path ptChangedFile = Paths.get(changedFile);
        Map<String, String> assemblyMap = configuration.getOptions().getDeploymentAssembly();
        for (String key : assemblyMap.keySet()) {
            Path ptSource = Paths.get(basePath + key);
            Path ptRelative = Paths.get(ptChangedFile.toString().substring(ptSource.toString().length()));
            return ptDocBase + ptRelative.toString();
        }
        return null;
    }
}
