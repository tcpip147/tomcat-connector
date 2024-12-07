package com.tcpip147.tomcatconnector;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.impl.ContentImpl;
import com.tcpip147.tomcatconnector.toolwindow.TomcatToolWindowContent;
import com.tcpip147.tomcatconnector.toolwindow.TomcatToolWindowFactory;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

public class OnStartUpActivity implements ProjectActivity {

    private static final Logger log = LoggerFactory.getLogger(OnStartUpActivity.class);

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                getRunningConfigurations(project, (configuration) -> {
                    Module module = configuration.getConfigurationModule().getModule();
                    if (module != null) {
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
                    }
                });
            }
        });

        project.getMessageBus().connect().subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
            @Override
            public void endUpdate() {
                reloadToolWindow(project);
            }
        });
        return null;
    }

    private void reloadToolWindow(Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Tomcat Servers");
        Content content = toolWindow.getContentManager().getContent(0);
        TomcatToolWindowContent toolWindowContent = content.getUserData(TomcatToolWindowFactory.TOOL_WINDOW_CONTENT);
        toolWindowContent.reload();
    }

    private void getChangedFiles(Module module, List<? extends VFileEvent> events, IChangedFileFilter filter) {
        String basePath = module.getProject().getBasePath();
        if (basePath != null) {
            for (VFileEvent event : events) {
                String changedFile = Objects.requireNonNull(event.getFile()).getPath();
                if (changedFile.startsWith(basePath)) {
                    filter.filtered(changedFile);
                }
            }
        }
    }

    private void getRunningConfigurations(Project project, IRunningConfigurationFilter filter) {
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.getRunningDescriptors(runnerAndConfigurationSettings -> {
            if (runnerAndConfigurationSettings.getConfiguration() instanceof TomcatConfiguration configuration) {
                if (configuration.isStarted()) {
                    filter.filtered(configuration);
                }
            }
            return false;
        });
    }

    private String getTargetFile(TomcatConfiguration configuration, String changedFile) {
        Module module = configuration.getConfigurationModule().getModule();
        if (module != null) {
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
