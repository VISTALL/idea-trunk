/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.tomcat;

import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.PositionManager;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessAdapter;
import com.intellij.debugger.engine.DefaultJSPPositionManager;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.javaee.run.localRun.CommandLineExecutableObject;
import com.intellij.javaee.run.localRun.ExecutableObject;
import com.intellij.javaee.serverInstances.DefaultJ2EEServerEvent;
import com.intellij.javaee.serverInstances.DefaultServerInstance;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class TomcatServerInstance extends DefaultServerInstance{
  private boolean myIsStartedUp = false;
  @NonNls protected static final String STARTED_SUFFIX = "org.apache.jk.server.JkMain start";
  @NonNls protected static final String STARTING_MESSAGE = "Starting service Tomcat-Apache";

  public TomcatServerInstance(CommonModel runConfiguration) {
    super(runConfiguration);
  }

  public void start(final ProcessHandler processHandler) {
    super.start(processHandler);
    fireServerListeners(new DefaultJ2EEServerEvent(true, false));

    final TomcatModel tomcatModel = (TomcatModel)getServerModel();
    DebuggerManager.getInstance(tomcatModel.getProject()).addDebugProcessListener(processHandler, new DebugProcessAdapter() {
      PositionManager positionManager;

      //executed in manager thread
      public void processDetached(DebugProcess process, boolean closedByUser) {
        super.processDetached(process, closedByUser);
        if(positionManager instanceof Tomcat40PositionManager) {
          ((Tomcat40PositionManager)positionManager).dispose();
        }
      }

      public void processAttached(DebugProcess process) {
        if(!tomcatModel.versionHigher(TomcatPersistentData.VERSION50)) {
          positionManager = new Tomcat40PositionManager(process, TomcatUtil.getGeneratedFilesPath(tomcatModel), getScopeFacets(getCommonModel()));
        }
        else {
          positionManager = new DefaultJSPPositionManager(process, getScopeFacets(getCommonModel())) {
            protected String getGeneratedClassesPackage() {
              return "org.apache.jsp";
            }
          };
        }
        process.appendPositionManager(positionManager);
      }
    });

    if(getCommonModel().isLocal()) {
      processHandler.addProcessListener(new ProcessAdapter() {
        public void onTextAvailable(ProcessEvent event, Key outputType) {
          final String text = event.getText();
          if(!myIsStartedUp && isStartingMessage(text)) {
            myIsStartedUp = true;
          }
        }
      });
    }
    else {
      myIsStartedUp = true;
    }
  }

  private boolean isStartingMessage(final String text) {
    final TomcatModel tomcatModel = ((TomcatModel)getServerModel());
    if (text.trim().endsWith(STARTED_SUFFIX)) {
      return true;
    }
    if (!tomcatModel.versionHigher(TomcatPersistentData.VERSION50)) {
      return text.contains(STARTING_MESSAGE);
    }
    if (text.contains(TomcatStartupPolicy.getDefaultCatalinaFileName() + " start")) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isStartupScriptTerminatesAfterServerStartup(@NotNull ExecutableObject startupScript) {
    if (startupScript instanceof CommandLineExecutableObject) {
      final String[] parameters = ((CommandLineExecutableObject)startupScript).getParameters();
      if (parameters.length >= 2) {
        String fileName = new File(parameters[0]).getName();
        if (TomcatStartupPolicy.getDefaultCatalinaFileName().equalsIgnoreCase(fileName) && parameters[1].equals("start")) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isConnected() {
    return myIsStartedUp && super.isConnected();
  }

}
