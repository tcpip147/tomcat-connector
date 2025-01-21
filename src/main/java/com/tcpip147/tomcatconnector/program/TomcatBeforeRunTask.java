package com.tcpip147.tomcatconnector.program;

import com.intellij.execution.BeforeRunTask;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public class TomcatBeforeRunTask extends BeforeRunTask<TomcatBeforeRunTask> {

    protected TomcatBeforeRunTask(@NotNull Key<TomcatBeforeRunTask> providerId) {
        super(providerId);
        setEnabled(true);
    }
}
