package org.jetbrains.idea.svn;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For exactly same refresh requests buffering:
 *
 * - refresh requests can be merged into one, but general principle is that each request should be reliably followed by refresh action 
 * - at the moment only one refresh action is being done
 * - if request had been submitted while refresh action was in progress, new refresh action is initiated right after first refresh action finishes
 *
 */
public class RequestsMerger {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.svn.RequestsMerger");
  private static final int ourDelay = 300;

  private final MyWorker myWorker;

  private final Object myLock = new Object();

  private MyState myState;
  private final Consumer<Runnable> myAlarm;
  
  private final List<Runnable> myWaitingStartListeners;
  private final List<Runnable> myWaitingFinishListeners;

  public RequestsMerger(final Runnable runnable, final Consumer<Runnable> alarm) {
    myAlarm = alarm;
    myWorker = new MyWorker(runnable);

    myState = MyState.empty;

    myWaitingStartListeners = new ArrayList<Runnable>();
    myWaitingFinishListeners = new ArrayList<Runnable>();
  }

  public void request() {
    LOG.debug("ext: request");
    doAction(MyAction.request);
  }

  public void waitRefresh(final Runnable runnable) {
    LOG.debug("ext: wait refresh");
    synchronized (myLock) {
      myWaitingStartListeners.add(runnable);
    }
    request();
  }

  public void ensureInitialization(final Runnable runnable) {
    LOG.debug("ext: ensure init");
    synchronized (myLock) {
      if (myWorker.isInitialized()) {
        runnable.run();
        return;
      }
      myWaitingStartListeners.add(runnable);
    }
    request();
  }

  private class MyWorker implements Runnable {
    private boolean myInitialized;
    private final Runnable myRunnable;

    private MyWorker(Runnable runnable) {
      myRunnable = runnable;
    }

    public void run() {
      LOG.debug("worker: started refresh");
      try {
        doAction(MyAction.start);
        myRunnable.run();
        synchronized (myLock) {
          myInitialized = true;
        }
      } finally {
        doAction(MyAction.finish);
      }
    }

    public boolean isInitialized() {
      return myInitialized;
    }
  }

  private void doAction(final MyAction action) {
    LOG.debug("doAction: START " + action.name());
    final MyExitAction[] exitActions;
    List<Runnable> toBeCalled = null;
    synchronized (myLock) {
      final MyState oldState = myState;
      myState = myState.transition(action);
      if (oldState.equals(myState)) return;
      exitActions = MyTransitionAction.getExit(oldState, myState);

      LOG.debug("doAction: oldState: " + oldState.name() + ", newState: " + myState.name());

      if (LOG.isDebugEnabled() && (exitActions != null)) {
        final String debugExitActions = StringUtil.join(exitActions, new Function<MyExitAction, String>() {
          public String fun(MyExitAction exitAction) {
            return exitAction.name();
          }
        }, " ");
        LOG.debug("exit actions: " + debugExitActions);
      }
      if (exitActions != null) {
        for (MyExitAction exitAction : exitActions) {
          if (MyExitAction.markStart.equals(exitAction)) {
            myWaitingFinishListeners.addAll(myWaitingStartListeners);
            myWaitingStartListeners.clear();
          } else if (MyExitAction.markEnd.equals(exitAction)) {
            toBeCalled = new ArrayList<Runnable>(myWaitingFinishListeners);
            myWaitingFinishListeners.clear();
          }
        }
      }
    }
    if (exitActions != null) {
      for (MyExitAction exitAction : exitActions) {
        if (MyExitAction.submitRequestToExecutor.equals(exitAction)) {
          myAlarm.consume(myWorker);
          //myAlarm.addRequest(myWorker, ourDelay);
          //ApplicationManager.getApplication().executeOnPooledThread(myWorker);
        }
      }
    }
    if (toBeCalled != null) {
      for (Runnable runnable : toBeCalled) {
        runnable.run();
      }
    }
    LOG.debug("doAction: END " + action.name());
  }

  private static enum MyState {
    empty() {
      @NotNull
      public MyState transition(MyAction action) {
        if (MyAction.request.equals(action)) {
          return MyState.requestSubmitted;
        }
        logWrongAction(this, action);
        return this;
      }},
    inProgress() {
      @NotNull
      public MyState transition(MyAction action) {
        if (MyAction.finish.equals(action)) {
          return MyState.empty;
        } else if (MyAction.request.equals(action)) {
          return MyState.inProgressRequestSubmitted;
        }
        logWrongAction(this, action);
        return this;
      }},
    inProgressRequestSubmitted() {
      @NotNull
      public MyState transition(MyAction action) {
        if (MyAction.finish.equals(action)) {
          return MyState.requestSubmitted;
        }
        if (MyAction.start.equals(action)) {
          logWrongAction(this, action);
        }
        return this;
      }},
    requestSubmitted() {
      @NotNull
      public MyState transition(MyAction action) {
        if (MyAction.start.equals(action)) {
          return MyState.inProgress;
        } else if (MyAction.finish.equals(action)) {
          // to be able to be started by another request
          logWrongAction(this, action);
          return MyState.empty;
        }
        return this;
      }};

    // under lock
    @NotNull
    public abstract MyState transition(final MyAction action);

    private static void logWrongAction(final MyState state, final MyAction action) {
      LOG.info("Wrong action: state=" + state.name() + ", action=" + action.name());
    }
  }

  private static class MyTransitionAction {
    private final static Map<Pair<MyState, MyState>, MyExitAction[]> myMap = new HashMap<Pair<MyState, MyState>, MyExitAction[]>();

    static {
      add(MyState.empty, MyState.requestSubmitted, MyExitAction.submitRequestToExecutor);
      add(MyState.requestSubmitted, MyState.inProgress, MyExitAction.markStart);
      add(MyState.inProgress, MyState.empty, MyExitAction.markEnd);
      add(MyState.inProgressRequestSubmitted, MyState.requestSubmitted, MyExitAction.submitRequestToExecutor, MyExitAction.markEnd);

      //... and not real but to be safe:
      add(MyState.inProgressRequestSubmitted, MyState.empty, MyExitAction.markEnd);
      add(MyState.inProgress, MyState.requestSubmitted, MyExitAction.markEnd);
    }

    private static void add(final MyState from , final MyState to, final MyExitAction... action) {
      myMap.put(new Pair<MyState, MyState>(from, to), action);
    }

    @Nullable
    public static MyExitAction[] getExit(final MyState from, final MyState to) {
      return myMap.get(new Pair<MyState, MyState>(from, to));
    }
  }

  private static enum MyExitAction {
    empty,
    submitRequestToExecutor,
    markStart,
    markEnd
  }

  private static enum MyAction {
    request,
    start,
    finish
  }
}
