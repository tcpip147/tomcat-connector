package com.tcpip147.tomcatconnector.program;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Key;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class TomcatBeforeRunProvider extends BeforeRunTaskProvider<TomcatBeforeRunTask> {

    private final Key<TomcatBeforeRunTask> ID = Key.create("TomcatBeforeRunTask");

    @Override
    public Key<TomcatBeforeRunTask> getId() {
        return ID;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getName() {
        return "Tomcat Deployment";
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.Actions.Compile;
    }

    @Override
    public @Nullable TomcatBeforeRunTask createTask(@NotNull RunConfiguration runConfiguration) {
        return new TomcatBeforeRunTask(ID);
    }

    @Override
    public boolean executeTask(@NotNull DataContext context, @NotNull RunConfiguration configuration, @NotNull ExecutionEnvironment environment, @NotNull TomcatBeforeRunTask task) {
        TomcatProgram program = new TomcatProgram();
        program.execute(environment);
        return true;
    }
}
