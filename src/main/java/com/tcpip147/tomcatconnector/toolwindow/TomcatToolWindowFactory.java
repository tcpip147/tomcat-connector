package com.tcpip147.tomcatconnector.toolwindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class TomcatToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final Key<TomcatToolWindowContent> TOOL_WINDOW_CONTENT = new Key<>("CONTENT");

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        TomcatToolWindowContent toolWindowContent = new TomcatToolWindowContent(project, toolWindow);
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
        content.putUserData(TOOL_WINDOW_CONTENT, toolWindowContent);
        toolWindow.getContentManager().addContent(content);
    }

}
