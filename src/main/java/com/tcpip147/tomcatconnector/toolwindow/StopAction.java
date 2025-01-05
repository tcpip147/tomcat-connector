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
import com.intellij.ui.components.JBList;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.execution.actions.StopAction.getActiveStoppableDescriptors;

public class StopAction extends AnAction {

    private final Project project;
    private final JBList<TomcatConfiguration> ltServer;

    public StopAction(Project project, JBList<TomcatConfiguration> ltServer) {
        super("Stop", "Stop", IconLoader.getIcon("/expui/run/stop.svg", StopAction.class));
        this.project = project;
        this.ltServer = ltServer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (ltServer.getSelectedIndex() > -1) {
            TomcatConfiguration tomcatConfiguration = ltServer.getSelectedValue();
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

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        if (ltServer.getSelectedIndex() > -1 && ltServer.getSelectedValue().isStarted()) {
            e.getPresentation().setEnabled(true);
            e.getPresentation().setText("Stop " + ltServer.getSelectedValue().getName());
        } else {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setText("Stop");
        }
        ltServer.repaint();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
