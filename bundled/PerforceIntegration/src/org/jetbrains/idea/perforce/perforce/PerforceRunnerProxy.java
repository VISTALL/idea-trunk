package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.containers.Convertor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class PerforceRunnerProxy {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.PerforceRunnerProxy");
  
  private final static String ourDefaultTitle = "Performing Perforce activity...";
  
  private final Map<String, Convertor<Object[], String>> myMethodName;
  private final PerforceRunnerI myProxy;
  
  private final Project myProject;

  public PerforceRunnerProxy(final Project project, final PerforceRunner runner) {
    myProject = project;

    myProxy = (PerforceRunnerI) Proxy.newProxyInstance(PerforceRunnerI.class.getClassLoader(), new Class[] {PerforceRunnerI.class},
                                     new MyPooledThreadProxy(runner));
    myMethodName = new HashMap<String, Convertor<Object[], String>>();
    fillNames();
  }

  private String assumeFirstParamP4File(final Object[] o) {
    if (o.length > 0 && o[0] instanceof P4File) {
      return ((P4File) o[0]).getLocalPath();
    } else {
      return "";
    }
  }

  // further we can possibly use passed parameters
  private void fillNames() {
    myMethodName.put("fstat", new Convertor<Object[], String>() {
      public String convert(Object[] o) {
        return MessageFormat.format("Retrieving file {0} info", assumeFirstParamP4File(o));
      }
    });
    myMethodName.put("edit", new Convertor<Object[], String>() {
      public String convert(Object[] o) {
        return MessageFormat.format("Opening file {0} for edit", assumeFirstParamP4File(o));
      }
    });
    myMethodName.put("revert", new Convertor<Object[], String>() {
      public String convert(Object[] o) {
        return MessageFormat.format("Reverting file {0} for edit", assumeFirstParamP4File(o));
      }
    });
    myMethodName.put("sync", new Convertor<Object[], String>() {
      public String convert(Object[] o) {
        return "Synchronizing Perforce view";
      }
    });
  }

  public PerforceRunnerI getProxy() {
    return myProxy;
  }

  private class MyPooledThreadProxy implements InvocationHandler {
    private final PerforceRunnerI myDelegate;
    private final ProgressManager myProgressManager;
    private final Application myApplication;

    public MyPooledThreadProxy(final PerforceRunnerI delegate) {
      myDelegate = delegate;
      myProgressManager = ProgressManager.getInstance();
      myApplication = ApplicationManager.getApplication();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (! (PerforceRunnerI.class.isAssignableFrom(method.getDeclaringClass()))) {
        LOG.info("Proxy used for wrong class's method invocation: " + method.getDeclaringClass().getCanonicalName());
        return null;
      }

      final String title;
      final Convertor<Object[], String> getter = myMethodName.get(method.getName());
      if (getter != null) {
        title = getter.convert(args);
      } else {
        title = ourDefaultTitle;
      }

      // invoke on delegate
      final MyActuallyInvoked invoked = new MyActuallyInvoked(myDelegate, method, args);

      if (myApplication.isDispatchThread()) {
        // todo what about cancellation?
        myProgressManager.runProcessWithProgressSynchronously(invoked, title, false, myProject);
      } else {
        invoked.run();
      }

      final VcsException vcsException = invoked.getVcsException();
      if (vcsException != null) {
        throw vcsException;
      }
      final RuntimeException runtimeException = invoked.getRuntimeException();
      if (runtimeException != null) {
        throw runtimeException;
      }

      return invoked.getResult();
    }
  }

  // todo more generic??
  private static class MyActuallyInvoked implements Runnable {
    private final Method myMethod;
    private final Object myProxy;
    private final Object[] myArgs;

    private VcsException myVcsException;
    private RuntimeException myRuntimeException;
    private Object myResult;

    private MyActuallyInvoked(final Object proxy, final Method method, final Object[] args) {
      super();
      myMethod = method;
      myProxy = proxy;
      myArgs = args;
    }

    public void run() {
      try {
        myResult = myMethod.invoke(myProxy, myArgs);
      }
      catch (IllegalAccessException e) {
        myRuntimeException = new RuntimeException(e);
      }
      catch (InvocationTargetException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof VcsException) {
          myVcsException = (VcsException) cause;
        } else if (cause instanceof RuntimeException) {
          myRuntimeException = (RuntimeException) cause;
        } else if (cause != null) {
          myRuntimeException = new RuntimeException(cause);
        }
      }
    }

    public VcsException getVcsException() {
      return myVcsException;
    }

    public RuntimeException getRuntimeException() {
      return myRuntimeException;
    }

    public Object getResult() {
      return myResult;
    }
  }
}
