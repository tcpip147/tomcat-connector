package com.tcpip147.tomcatconnector;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;

import javax.swing.*;

public class TomcatConfigurationType extends ConfigurationTypeBase {

    static final String ID = "TomcatConfiguration";
    static final Icon ICON = IconLoader.getIcon("/icon/server.svg", TomcatConfigurationType.class);

    TomcatConfigurationType() {
        super(ID, "Tomcat", "Tomcat Configuration Type", NotNullLazyValue.createValue(() -> ICON));
        addFactory(new TomcatConfigurationFactory(this));
    }

}
