package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.impl.ExecutionManagerImpl;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.treeStructure.SimpleTree;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

import static com.intellij.execution.actions.StopAction.getActiveStoppableDescriptors;

public class StopAction extends AnAction {

    private final Project project;
    private final SimpleTree ltServer;

    public StopAction(Project project, SimpleTree ltServer) {
        super("Stop", "Stop", IconLoader.getIcon("/expui/run/stop.svg", StopAction.class));
        this.project = project;
        this.ltServer = ltServer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (ltServer.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode node) {
            if (node.getUserObject() instanceof TomcatConfiguration tomcatConfiguration) {
                List<RunnerAndConfigurationSettings> runnerAndConfigurationSettingsList = RunManager.getInstance(project).getAllSettings();
                for (RunnerAndConfigurationSettings runnerAndConfigurationSettings : runnerAndConfigurationSettingsList) {
                    if (runnerAndConfigurationSettings.getConfiguration() == tomcatConfiguration) {
                        List<RunContentDescriptor> stoppableDescriptors = getActiveStoppableDescriptors(project);
                        for (RunContentDescriptor descriptor : stoppableDescriptors) {
                            if (descriptor.getDisplayName().equals(tomcatConfiguration.getName())) {
                                ExecutionManagerImpl.stopProcess(descriptor);
                            }
                        }
                        this.update(e);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        TomcatConfiguration selected = null;
        if (ltServer.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode node) {
            if (node.getUserObject() instanceof TomcatConfiguration tomcatConfiguration) {
                selected = tomcatConfiguration;
            }
        }
        if (selected != null && selected.isStarted()) {
            e.getPresentation().setEnabled(true);
            e.getPresentation().setText("Stop " + selected.getName());
        } else {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setText("Stop");
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
