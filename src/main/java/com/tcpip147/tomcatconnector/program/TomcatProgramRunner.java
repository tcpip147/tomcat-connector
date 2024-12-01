package com.tcpip147.tomcatconnector.program;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.tcpip147.tomcatconnector.TomcatConfiguration;
import org.jetbrains.annotations.NotNull;

public class TomcatProgramRunner extends DefaultJavaProgramRunner {

    private static final String RUNNER_ID = "TomcatProgramRunner";

    @NotNull
    @Override
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
        return (DefaultRunExecutor.EXECUTOR_ID.equals(executorId)) && runProfile instanceof TomcatConfiguration;
    }

    private void runTomcatServer(ExecutionEnvironment environment) throws ExecutionException {
        super.execute(environment);
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        new TomcatProgram().execute(environment, () -> {
            try {
                runTomcatServer(environment);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
