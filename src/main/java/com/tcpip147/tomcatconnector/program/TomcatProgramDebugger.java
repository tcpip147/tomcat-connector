package com.tcpip147.tomcatconnector.program;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

public class TomcatProgramDebugger extends GenericDebuggerRunner {

    private static final String RUNNER_ID = "TomcatProgramDebugger";

    @NotNull
    @Override
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
        return (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)) && runProfile instanceof TomcatConfiguration;
    }
}
