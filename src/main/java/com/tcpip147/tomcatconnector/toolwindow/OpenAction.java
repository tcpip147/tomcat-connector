package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBList;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class OpenAction extends AnAction {

    private final JBList<TomcatConfiguration> ltServer;

    public OpenAction(JBList<TomcatConfiguration> ltServer) {
        super("Open Configurations in Explorer");
        this.ltServer = ltServer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (Path.of(ltServer.getSelectedValue().getOptions().getCatalinaBase(), "conf").toFile().exists()) {
            RevealFileAction.openDirectory(Path.of(ltServer.getSelectedValue().getOptions().getCatalinaBase(), "conf"));
        }
    }
}
