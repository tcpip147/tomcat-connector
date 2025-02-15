package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleTree;
import org.jetbrains.annotations.NotNull;

public class RefreshAction extends AnAction {

    private final Project project;
    private final TomcatToolWindowContent toolWindowContent;

    public RefreshAction(Project project, TomcatToolWindowContent toolWindowContent) {
        super("Refresh", "Refresh", AllIcons.General.Refresh);
        this.project = project;
        this.toolWindowContent = toolWindowContent;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        toolWindowContent.reload();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
