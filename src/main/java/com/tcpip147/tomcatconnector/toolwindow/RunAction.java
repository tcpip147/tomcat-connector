package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.treeStructure.SimpleTree;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.List;

public class RunAction extends AnAction {

    private final Project project;
    private final SimpleTree ltServer;

    public RunAction(Project project, SimpleTree ltServer) {
        super("Run", "Run", IconLoader.getIcon("/expui/run/run.svg", RunAction.class));
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
            e.getPresentation().setIcon(IconLoader.getIcon("/expui/run/rerun.svg", RunAction.class));
        } else {
            e.getPresentation().setIcon(IconLoader.getIcon("/expui/run/run.svg", RunAction.class));
        }
        if (selected != null) {
            e.getPresentation().setEnabled(true);
            e.getPresentation().setText("Run " + selected.getName());
        } else {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setText("Run");
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
