package org.jetbrains.idea.tomcat;

import com.intellij.debugger.engine.SuspendContextImpl;
import com.intellij.debugger.engine.SuspendContextRunnable;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.run.J2EEServerTestCase;

import java.io.File;

/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
public class Tomcat50Test extends J2EEServerTestCase{

  public Tomcat50Test() {
    super(PathManagerEx.getTestDataPath() + File.separator + "j2ee/j2eeExecution", "testJ2EEProject", "Tomcat 5");
  }

  protected void initApplication() throws Exception {
    initPathVariable("TOMCAT5_HOME", PathManager.getHomePath() + File.separator + "tools" + File.separator + "tomcat" + File.separator + "tomcat-5.0.28");
    super.initApplication();
  }

  public void testTomcat5() throws InterruptedException {
    executeConfiguration();

    onBreakpoint(new SuspendContextRunnable() {
      public void run(SuspendContextImpl suspendContext) throws Exception {
        printContext(suspendContext);
        getDebugProcess().getManagerThread().schedule(getDebugProcess().createStepOverCommand(suspendContext, false));
      }
    });

    onBreakpoint(new SuspendContextRunnable() {
      public void run(SuspendContextImpl suspendContext) throws Exception {
        printContext(suspendContext);
        resume(suspendContext);
      }
    });

    runApplication();
  }

}
