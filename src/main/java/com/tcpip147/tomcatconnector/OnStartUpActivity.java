package com.tcpip147.tomcatconnector;

import com.intellij.execution.ExecutionManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class OnStartUpActivity implements ProjectActivity {

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        project.getMessageBus().connect().subscribe(
                VirtualFileManager.VFS_CHANGES,
                new BulkFileListener() {
                    @Override
                    public void after(@NotNull List<? extends VFileEvent> events) {
                        getRunningConfigurations(project, (configuration) -> {
                            Module module = configuration.getConfigurationModule().getModule();
                            getChangedFiles(module, events, (file) -> {
                                File sourceFile = new File(file);
                                String targetFilePath = getTargetFile(configuration, file);
                                if (targetFilePath != null) {
                                    File targetFile = new File(targetFilePath);
                                    try {
                                        if (!sourceFile.exists()) {
                                            FileUtil.delete(targetFile);
                                        } else {
                                            FileUtil.copyFileOrDir(sourceFile, targetFile);
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        });
                    }
                });
        return null;
    }

    private void getChangedFiles(Module module, List<? extends VFileEvent> events, IChangedFileFilter filter) {
        String basePath = module.getProject().getBasePath();
        ListIterator<? extends VFileEvent> iterator = events.listIterator();
        while (iterator.hasNext()) {
            VFileEvent event = iterator.next();
            String changedFile = event.getFile().getPath();
            if (changedFile.startsWith(basePath)) {
                filter.filtered(changedFile);
            }
        }
    }

    private void getRunningConfigurations(Project project, IRunningConfigurationFilter filter) {
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.getRunningDescriptors(runnerAndConfigurationSettings -> {
            if (runnerAndConfigurationSettings.getConfiguration() instanceof TomcatConfiguration) {
                TomcatConfiguration configuration = (TomcatConfiguration) runnerAndConfigurationSettings.getConfiguration();
                if (configuration.isStarted()) {
                    filter.filtered(configuration);
                }
            }
            return false;
        });
    }

    private String getTargetFile(TomcatConfiguration configuration, String changedFile) {
        Module module = configuration.getConfigurationModule().getModule();
        String basePath = module.getProject().getBasePath();
        Path ptDocBase = Paths.get(configuration.getOptions().getDocBase());
        Path ptChangedFile = Paths.get(changedFile);
        Map<String, String> assemblyMap = configuration.getOptions().getDeploymentAssembly();
        for (String key : assemblyMap.keySet()) {
            Path ptSourceBase = Paths.get(basePath + key);
            if (ptChangedFile.toString().startsWith(ptSourceBase.toString())) {
                Path ptRelative = Paths.get(ptChangedFile.toString().substring(ptSourceBase.toString().length()));
                return ptDocBase + assemblyMap.get(key) + ptRelative;
            }
        }
        return null;
    }

    private interface IRunningConfigurationFilter {
        void filtered(TomcatConfiguration configuration);
    }

    private interface IChangedFileFilter {
        void filtered(String changedFile);
    }
}
