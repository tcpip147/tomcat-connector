package com.tcpip147.tomcatconnector;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;
import com.intellij.serialization.stateProperties.MapStoredProperty;

import java.util.Map;

public class TomcatConfigurationOptions extends RunConfigurationOptions {

    private final StoredProperty<String> catalinaHome = string("").provideDelegate(this, "catalinaHome");
    private final StoredProperty<String> catalinaBase = string("").provideDelegate(this, "catalinaBase");
    private final StoredProperty<String> docBase = string("").provideDelegate(this, "docBase");
    private final StoredProperty<String> vmOptions = string("").provideDelegate(this, "vmOptions");
    private final MapStoredProperty<String, String> deploymentAssembly = (MapStoredProperty<String, String>) this.<String, String>linkedMap().provideDelegate(this, "deploymentAssembly");

    public String getCatalinaHome() {
        return catalinaHome.getValue(this);
    }

    public void setCatalinaHome(String value) {
        catalinaHome.setValue(this, value);
    }

    public String getCatalinaBase() {
        return catalinaBase.getValue(this);
    }

    public void setCatalinaBase(String value) {
        catalinaBase.setValue(this, value);
    }

    public String getDocBase() {
        return docBase.getValue(this);
    }

    public void setDocBase(String value) {
        docBase.setValue(this, value);
    }

    public String getVmOptions() {
        return vmOptions.getValue(this);
    }

    public void setVmOptions(String value) {
        vmOptions.setValue(this, value);
    }

    public Map<String, String> getDeploymentAssembly() {
        return deploymentAssembly.getValue(this);
    }

    public void setDeploymentAssembly(Map<String, String> value) {
        deploymentAssembly.setValue(this, value);
    }
}
