package com.tcpip147.tomcatconnector;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TomcatConfigurationFactory extends ConfigurationFactory {

    protected TomcatConfigurationFactory(TomcatConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull String getId() {
        return TomcatConfigurationType.ID;
    }

    @NotNull
    @Override
    public TomcatConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new TomcatConfiguration(project, this, "Tomcat");
    }

    @Nullable
    @Override
    public Class<? extends BaseState> getOptionsClass() {
        return TomcatConfigurationOptions.class;
    }
}
