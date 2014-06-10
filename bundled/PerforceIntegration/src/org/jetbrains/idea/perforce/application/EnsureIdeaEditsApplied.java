package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

/**
 * since single thread executor used in PerforceManager
 */
public class EnsureIdeaEditsApplied {
  private final PerforceManager myManager;
  private final PerforceSettings mySettings;
  private static final int DEFAULT = 20000;
  private int myTimeout;

  private final static ThreadLocal<Object> myGuard = new ThreadLocal<Object>();

  public EnsureIdeaEditsApplied(final Project project) {
    myManager = PerforceManager.getInstance(project);
    mySettings = PerforceSettings.getSettings(project);
  }

  public void ensure(final Runnable toBeExcecutedSynchronouslyOnTheQueueThread) {
    // do not allow to wait from task that already in queue -> it's a deadlock
    assert myGuard.get() == null;
    myGuard.set(new Object());

    try {
      final Semaphore waitLock = new Semaphore();
      final Semaphore executingLock = new Semaphore();

      myTimeout = mySettings.SERVER_TIMEOUT > 0 ? DEFAULT : mySettings.SERVER_TIMEOUT;

      final Runnable callback = new Runnable() {
        public void run() {
          executingLock.down();
          waitLock.up();
          executingLock.waitFor(myTimeout * 10);
        }
      };
      waitLock.down();
      myManager.queueUpdateRequest(callback);
      waitLock.waitFor(myTimeout * 3);
      try {
        toBeExcecutedSynchronouslyOnTheQueueThread.run();
      } finally {
        executingLock.up();
      }
    } finally {
      myGuard.set(null);
    }
  }
}
