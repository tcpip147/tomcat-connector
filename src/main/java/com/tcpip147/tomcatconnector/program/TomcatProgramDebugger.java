package com.tcpip147.tomcatconnector.program;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
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

    private void runTomcatServer(ExecutionEnvironment environment) throws ExecutionException {
        super.execute(environment);
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) {
        new TomcatProgram().execute(environment, () -> {
            try {
                runTomcatServer(environment);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
