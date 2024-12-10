package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class OpenAction extends AnAction {

    private Project project;
    private JBList<TomcatConfiguration> ltServer;

    public OpenAction(Project project, JBList<TomcatConfiguration> ltServer) {
        super("Open Configurations in Explorer");
        this.project = project;
        this.ltServer = ltServer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (Path.of(ltServer.getSelectedValue().getOptions().getCatalinaBase(), "conf").toFile().exists()) {
            RevealFileAction.openDirectory(Path.of(ltServer.getSelectedValue().getOptions().getCatalinaBase(), "conf"));
        }
    }
}
