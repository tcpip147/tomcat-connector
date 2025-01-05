package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBList;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RunAction extends AnAction {

    private final Project project;
    private final JBList<TomcatConfiguration> ltServer;

    public RunAction(Project project, JBList<TomcatConfiguration> ltServer) {
        super("Run", "Run", IconLoader.getIcon("/expui/run/run.svg", RunAction.class));
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
                    if (tomcatConfiguration.isStarted()) {
                        ExecutionManager.getInstance(project).restartRunProfile(project,
                                DefaultRunExecutor.getRunExecutorInstance(),
                                DefaultExecutionTarget.INSTANCE,
                                runnerAndConfigurationSettings,
                                tomcatConfiguration.getHandler());
                    } else {
                        RunManager.getInstance(project).setSelectedConfiguration(runnerAndConfigurationSettings);
                        ProgramRunnerUtil.executeConfiguration(runnerAndConfigurationSettings, DefaultRunExecutor.getRunExecutorInstance());
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
            e.getPresentation().setIcon(IconLoader.getIcon("/expui/run/rerun.svg", RunAction.class));
        } else {
            e.getPresentation().setIcon(IconLoader.getIcon("/expui/run/run.svg", RunAction.class));
        }
        if (ltServer.getSelectedIndex() > -1) {
            e.getPresentation().setEnabled(true);
            e.getPresentation().setText("Run " + ltServer.getSelectedValue().getName());
        } else {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setText("Run");
        }
        ltServer.repaint();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
