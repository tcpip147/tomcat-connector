package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TomcatToolWindowContent {

    private final Project project;
    private final ToolWindow toolWindow;
    private final JPanel pContent;
    private JBList<TomcatConfiguration> ltServer;
    private DefaultListModel<TomcatConfiguration> model;
    private StopAction stopAction;
    private ActionPopupMenu actionPopupMenu;

    public TomcatToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        pContent = new JPanel();
        pContent.setLayout(new BorderLayout());
        createList();
        createToolbar();
        createContextMenu();
        reload();
    }

    private void createContextMenu() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RunAction(project, ltServer));
        group.add(new StopAction(project, ltServer));
        group.addSeparator();
        group.add(new OpenAction(project, ltServer));
        actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("TomcatToolWindowPopupMenu", group);
    }

    private void createToolbar() {
        List<AnAction> actionList = new ArrayList<>();
        actionList.add(new RunAction(project, ltServer));
        stopAction = new StopAction(project, ltServer);
        actionList.add(stopAction);
        toolWindow.getComponent().putClientProperty(ToolWindowContentUi.DONT_HIDE_TOOLBAR_IN_HEADER, true);
        toolWindow.setTitleActions(actionList);
    }

    private void createList() {
        model = new DefaultListModel<>();
        ltServer = new JBList<>(model);
        ltServer.setCellRenderer(new TomcatListCellRenderer());
        ltServer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        ltServer.addListSelectionListener(e -> {
            if (ltServer.getSelectedIndex() > -1) {
                //
            }
        });
        ltServer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = ltServer.locationToIndex(e.getPoint());
                    if (index > -1) {
                        ltServer.setSelectionInterval(index, index);
                        actionPopupMenu.getComponent().show(toolWindow.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        JPanel pListWrapper = new JPanel();
        pListWrapper.setLayout(new GridLayout(0, 1));
        pListWrapper.add(ltServer);
        JBScrollPane panel = new JBScrollPane(pListWrapper);
        panel.setBorder(BorderFactory.createEmptyBorder());
        pContent.add(panel, BorderLayout.CENTER);
    }

    public void reload() {
        model.removeAllElements();
        List<RunConfiguration> configurationList = RunManager.getInstance(project).getAllConfigurationsList();
        for (RunConfiguration configuration : configurationList) {
            if (configuration instanceof TomcatConfiguration tomcatConfiguration) {
                model.addElement(tomcatConfiguration);
            }
        }
    }

    public JPanel getContentPanel() {
        return pContent;
    }

    private static class TomcatListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            TomcatConfiguration tomcatConfiguration = (TomcatConfiguration) value;
            String name = tomcatConfiguration.getName() + (tomcatConfiguration.isStarted() ? " [Started]" : " [Stopped]");
            return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        }
    }
}
