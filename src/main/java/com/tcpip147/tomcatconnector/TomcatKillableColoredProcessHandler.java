package com.tcpip147.tomcatconnector;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.util.io.BaseOutputReader;
import org.jetbrains.annotations.NotNull;

public class TomcatKillableColoredProcessHandler extends KillableColoredProcessHandler {

    public TomcatKillableColoredProcessHandler(@NotNull GeneralCommandLine commandLine) throws ExecutionException {
        super(commandLine);
    }

    @NotNull
    @Override
    protected BaseOutputReader.Options readerOptions() {
        return BaseOutputReader.Options.forMostlySilentProcess();
    }
}
