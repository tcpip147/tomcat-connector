package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TomcatToolWindowContent {

    private final Project project;
    private final ToolWindow toolWindow;
    private final JPanel pContent;
    private SimpleTree ltServer;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel model;
    private RunAction runAction;
    private StopAction stopAction;
    private ActionPopupMenu actionPopupMenu;
    private JBScrollPane scrollPane;

    public TomcatToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        pContent = new JPanel();
        pContent.setLayout(new BorderLayout());
        createList();
        createToolbar();
        // createContextMenu();
        reload();
    }

    private void createContextMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RunAction(project, ltServer));
        group.add(new StopAction(project, ltServer));
        group.addSeparator();
        group.add(new OpenAction(ltServer));
        actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("TomcatToolWindowPopupMenu", group);
    }

    private void createToolbar() {
        List<AnAction> actionList = new ArrayList<>();
        runAction = new RunAction(project, ltServer);
        actionList.add(runAction);
        stopAction = new StopAction(project, ltServer);
        actionList.add(stopAction);
        toolWindow.getComponent().putClientProperty(ToolWindowContentUi.DONT_HIDE_TOOLBAR_IN_HEADER, true);
        toolWindow.setTitleActions(actionList);
    }

    private void createList() {
        root = new DefaultMutableTreeNode("Root");
        model = new DefaultTreeModel(root, true);
        ltServer = new SimpleTree();
        ltServer.setRootVisible(false);
        ltServer.setModel(model);
        DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ltServer.setSelectionModel(selectionModel);
        ltServer.setCellRenderer(new TreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (value instanceof DefaultMutableTreeNode node) {
                    if (node.getUserObject() instanceof TomcatConfiguration configuration) {
                        return new JBLabel(configuration.getName() + (configuration.isStarted() ? " [Started]" : " [Stopped]"));
                    } else if (node.getUserObject() instanceof File file) {
                        return new JBLabel(file.getName());
                    }
                }
                return new JBLabel(String.valueOf(value));
            }
        });

        ltServer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (ltServer.getLastSelectedPathComponent() instanceof DefaultMutableTreeNode node) {
                        if (node.getUserObject() instanceof File file) {
                            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
                            FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile, 0), true);
                        }
                    }
                }
            }
        });

        /*
        ltServer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = ltServer.getMaxSelectionRow();
                    if (index > -1) {
                        ltServer.setSelectionInterval(index, index);
                        actionPopupMenu.getComponent().show(toolWindow.getComponent(), e.getX(), e.getY() - scrollPane.getVerticalScrollBar().getValue());
                    }
                }
            }
        });
        */

        JPanel pListWrapper = new JPanel();
        pListWrapper.setLayout(new

                GridLayout(0, 1));
        pListWrapper.add(ltServer);
        scrollPane = new JBScrollPane(pListWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        pContent.add(scrollPane, BorderLayout.CENTER);
    }

    public void reload() {
        root.removeAllChildren();
        List<RunConfiguration> configurationList = RunManager.getInstance(project).getAllConfigurationsList();
        for (RunConfiguration configuration : configurationList) {
            if (configuration instanceof TomcatConfiguration tomcatConfiguration) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(tomcatConfiguration);
                Path confPath = Paths.get(tomcatConfiguration.getOptions().getCatalinaBase(), "conf");
                for (File file : confPath.toFile().listFiles()) {
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file);
                    childNode.setAllowsChildren(false);
                    node.add(childNode);
                }
                root.add(node);
            }
        }
        model.reload();
    }

    public JPanel getContentPanel() {
        return pContent;
    }
}
