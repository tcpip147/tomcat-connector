package com.tcpip147.tomcatconnector;

import com.intellij.configurationStore.XmlSerializer;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TomcatConfiguration extends RunConfigurationBase<TomcatConfigurationOptions> {

    private final RunConfigurationModule configurationModule;
    private boolean isStarted = false;
    private TomcatKillableColoredProcessHandler handler;

    public TomcatConfiguration(Project project, TomcatConfigurationFactory factory, String name) {
        super(project, factory, name);
        configurationModule = new RunConfigurationModule(project);
    }

    public RunConfigurationModule getConfigurationModule() {
        return configurationModule;
    }

    @NotNull
    @Override
    public TomcatConfigurationOptions getOptions() {
        return (TomcatConfigurationOptions) super.getOptions();
    }

    @NotNull
    @Override
    public TomcatSettingEditor getConfigurationEditor() {
        return new TomcatSettingEditor(this);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        if (getOptions() != null) {
            XmlSerializer.deserializeInto(element, getOptions());
            configurationModule.readExternal(element);
        }
    }

    @Override
    public void writeExternal(@NotNull Element element) {
        super.writeExternal(element);
        if (getOptions() != null) {
            XmlSerializer.serializeObjectInto(getOptions(), element);
            if (configurationModule.getModule() != null) {
                configurationModule.writeExternal(element);
            }
        }
    }

    @Nullable
    @Override
    public CommandLineState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new TomcatJavaCommandLineState(this, environment);
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public void setHandler(TomcatKillableColoredProcessHandler handler) {
        this.handler = handler;
    }

    public TomcatKillableColoredProcessHandler getHandler() {
        return handler;
    }
}
