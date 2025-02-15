package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.SimpleTree;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.nio.file.Path;

public class OpenAction extends AnAction {

    private final SimpleTree ltServer;

    public OpenAction(SimpleTree ltServer) {
        super("Open Configurations in Explorer");
        this.ltServer = ltServer;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (ltServer.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode node) {
            if (node.getUserObject() instanceof TomcatConfiguration tomcatConfiguration) {
                if (Path.of(tomcatConfiguration.getOptions().getCatalinaBase(), "conf").toFile().exists()) {
                    RevealFileAction.openDirectory(Path.of(tomcatConfiguration.getOptions().getCatalinaBase(), "conf"));
                }
            }
        }
    }
}
