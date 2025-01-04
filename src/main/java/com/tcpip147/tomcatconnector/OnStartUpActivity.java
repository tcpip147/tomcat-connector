package com.tcpip147.tomcatconnector;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.tcpip147.tomcatconnector.toolwindow.TomcatToolWindowContent;
import com.tcpip147.tomcatconnector.toolwindow.TomcatToolWindowFactory;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OnStartUpActivity implements ProjectActivity {

    private static final Logger log = LoggerFactory.getLogger(OnStartUpActivity.class);

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        // Auto redeployment on saving
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                getRunningConfigurations(project, (configuration) -> {
                    getChangedFiles(project, events, new IChangedFileFilter() {
                        @Override
                        public void filtered(String changedFile) {
                            String targetFilePath = getTargetFile(project, changedFile, configuration);
                            File sourceFile = new File(changedFile);
                            File targetFile = new File(Objects.requireNonNull(targetFilePath));
                            try {
                                FileUtil.copy(sourceFile, targetFile);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                });
            }
        });

        // Server status in toolWindow
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

    private void getChangedFiles(Project project, List<? extends VFileEvent> events, IChangedFileFilter filter) {
        String basePath = project.getBasePath();
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
        ExecutionManager.getInstance(project).getRunningDescriptors(runnerAndConfigurationSettings -> {
            if (runnerAndConfigurationSettings.getConfiguration() instanceof TomcatConfiguration configuration) {
                if (configuration.isStarted()) {
                    filter.filtered(configuration);
                }
            }
            return false;
        });
    }

    private String getTargetFile(Project project, String changedFile, TomcatConfiguration configuration) {
        String basePath = project.getBasePath();
        Map<String, String> assemblyMap = configuration.getOptions().getDeploymentAssembly();
        for (String key : assemblyMap.keySet()) {
            if (changedFile.startsWith(basePath + key)) {
                String relativePath = changedFile.substring((basePath + key).length());
                return configuration.getOptions().getCatalinaBase() + assemblyMap.get(key) + relativePath;
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
