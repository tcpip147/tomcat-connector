package com.tcpip147.tomcatconnector;

import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class TomcatJavaCommandLineState extends JavaCommandLineState {

    private final TomcatConfiguration configuration;

    public TomcatJavaCommandLineState(TomcatConfiguration configuration, ExecutionEnvironment environment) {
        super(environment);
        this.configuration = configuration;
    }

    @Override
    protected JavaParameters createJavaParameters() throws ExecutionException {
        Path ptCatalinaHome = Path.of(configuration.getOptions().getCatalinaHome());
        Path ptCatalinaBase = Path.of(configuration.getOptions().getCatalinaBase());

        ProjectRootManager manager = ProjectRootManager.getInstance(configuration.getProject());
        JavaParameters javaParams = new JavaParameters();
        javaParams.setDefaultCharset(configuration.getProject());
        javaParams.setWorkingDirectory(ptCatalinaBase.toFile());
        javaParams.setJdk(manager.getProjectSdk());
        javaParams.getClassPath().add(ptCatalinaHome.resolve("bin/bootstrap.jar").toFile());
        javaParams.getClassPath().add(ptCatalinaHome.resolve("bin/tomcat-juli.jar").toFile());
        javaParams.setMainClass("org.apache.catalina.startup.Bootstrap");
        javaParams.getProgramParametersList().add("start");

        ParametersList vmParams = javaParams.getVMParametersList();
        vmParams.addProperty("catalina.home", ptCatalinaHome.toString());
        vmParams.defineProperty("catalina.base", ptCatalinaBase.toString());
        vmParams.defineProperty("java.io.tmpdir", ptCatalinaBase.resolve("temp").toString());
        vmParams.addParametersString(configuration.getOptions().getVmOptions());

        return javaParams;
    }

    @Override
    protected @NotNull OSProcessHandler startProcess() throws ExecutionException {
        TomcatKillableColoredProcessHandler handler = new TomcatKillableColoredProcessHandler(createCommandLine());
        handler.setShouldKillProcessSoftly(!DebuggerSettings.getInstance().KILL_PROCESS_IMMEDIATELY);
        ProcessTerminatedListener.attach(handler);
        handler.addProcessListener(new ProcessAdapter() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {
                super.startNotified(event);
                configuration.setStarted(true);
                configuration.setHandler(handler);
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                super.processTerminated(event);
                configuration.setStarted(false);
            }
        });
        return handler;
    }
}
