package org.intellij.j2ee.web.resin;

import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.PositionManager;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessAdapter;
import com.intellij.debugger.engine.DefaultJSPPositionManager;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.serverInstances.DefaultJ2EEServerEvent;
import com.intellij.javaee.serverInstances.DefaultServerInstance;
import com.intellij.openapi.util.io.FileUtil;
import org.intellij.j2ee.web.resin.resin.ResinConfiguration;
import org.intellij.j2ee.web.resin.resin.version.ResinVersion;

import java.io.File;
import java.io.StringWriter;

public class ResinServerInstance extends DefaultServerInstance {

    public ResinServerInstance(CommonModel runConfiguration) {
        super(runConfiguration);
    }

    public void start(final ProcessHandler processHandler) {
        super.start(processHandler);
        fireServerListeners(new DefaultJ2EEServerEvent(true, false));

        final ResinModel resinModel = (ResinModel) getServerModel();
        DebuggerManager.getInstance(resinModel.getProject()).addDebugProcessListener(processHandler, new DebugProcessAdapter() {
            PositionManager positionManager;

            //executed in manager thread
            public void processDetached(DebugProcess process, boolean closedByUser) {
                super.processDetached(process, closedByUser);
            }

            public void processAttached(final DebugProcess process) {
                try {
                    final ResinConfiguration configuration;
                    configuration = resinModel.getResinConfiguration(true);
                    if(resinModel.DEBUG_CONFIGURATION) {
                      StringWriter sw = new StringWriter()
                        .append("\n---\n").append(ResinBundle.message("message.text.resin.conf.debug", configuration.getConfPath())).append("\n");
                      sw.append(new String(FileUtil.loadFileText(new File(configuration.getConfPath()))));
                      sw.append("\n---\n");
                      processHandler.notifyTextAvailable(sw.toString(), ProcessOutputTypes.SYSTEM);
                    }
                    if (configuration.getResinInstallation().getVersion() == ResinVersion.VERSION_2_X) {
                        positionManager = null;
                    }
                    else {
                        //TODO SCF compiler bug ¿?
                        positionManager = new DefaultJSPPositionManager(process, getScopeFacets(getCommonModel())) {
                            protected String getGeneratedClassesPackage() {
                                return "_jsp";
                            }
                        };
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
                
                if (positionManager != null)
                    process.appendPositionManager(positionManager);
            }
        });
    }

    public void shutdown() {
        super.shutdown();
      ProcessHandler processHandler = getProcessHandler();
      if (processHandler instanceof OSProcessHandler) ((OSProcessHandler)processHandler).getProcess().destroy();
    }
}
