package org.jetbrains.idea.perforce.perforce.login;

public abstract class ReactivableSingleLooped {
  protected final static long DELAY = 1000;

  private final Object myLock;
  private boolean myActive;
  private boolean myScheduled;
  private final MyInvoker myInvoker;

  public ReactivableSingleLooped() {
    myLock = new Object();
    myInvoker = new MyInvoker() {
      public void executeInLoop() {
        executeInLoopImpl();
      }

      public void executeOnce() {
        executeOnceImpl();
      }
    };
  }

  protected abstract void schedule(final long timeout, final Runnable runnable);

  /**
   * @return re-schedule timeout
   */
  protected abstract long runImpl();
  /**
   * @return default re-schedule timeout
   */
  protected abstract long getDefaultTimeout();

  public void start() {
    synchronized (myLock) {
      myActive = true;
      schedule(DELAY, new MyScheduledRunnable(! myScheduled, myInvoker));
      // schedule will schedule if had not been scheduled before :)
      myScheduled = true;
    }
  }

  public void stop() {
    synchronized (myLock) {
      myActive = false;
    }
  }

  public void executeInLoopImpl() {
    synchronized (myLock) {
      if (! myActive) {
        myScheduled = false;  // loop disconnected
        return;
      }
      // just for sure
      myScheduled = true;
    }
    long timeout = getDefaultTimeout();
    try {
      timeout = runImpl();
    } finally {
      synchronized (myLock) {
        if (myActive) {
          schedule(timeout, new MyScheduledRunnable(true, myInvoker));
        } else {
          myScheduled = false;
        }
      }
    }
  }

  public void executeOnceImpl() {
    synchronized (myLock) {
      if (! myActive) return;
    }
    runImpl();
  }

  private static class MyScheduledRunnable implements Runnable {
    private final boolean myScheduleLoop;
    private final MyInvoker myMaster;

    protected MyScheduledRunnable(boolean scheduleLoop, MyInvoker master) {
      myScheduleLoop = scheduleLoop;
      myMaster = master;
    }

    public void run() {
      if (myScheduleLoop) {
        myMaster.executeInLoop();
      } else {
        myMaster.executeOnce();
      }
    }
  }

  private interface MyInvoker {
    void executeInLoop();
    void executeOnce();
  }
}
