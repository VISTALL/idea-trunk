package org.jetbrains.idea.perforce;

import com.intellij.openapi.util.Ref;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.concurrency.Semaphore;
import junit.framework.Assert;
import org.jetbrains.idea.perforce.perforce.login.ReactivableSingleLooped;

public class ReactivableSingleLoopedTest extends CodeInsightFixtureTestCase {
  private MyFictiveScheduler myScheduler;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myScheduler = new MyFictiveScheduler();
  }

  public void testSimple() throws Exception {
    final MyReactivableSingleLooped looped = new MyReactivableSingleLooped(myScheduler);
    looped.start();
    looped.stop();
    // must be something in a queue
    myScheduler.assertScheduled();
  }

  public void testReactivate() throws Exception {
    final MyReactivableSingleLooped looped = new MyReactivableSingleLooped(myScheduler);

    // first start goes immediately.. for test
    int invocCount = 0;
    for (int i = 0; i < 4; i++) {
      looped.start();
      // each start adds invocation but in queue there's still first request 
      ++ invocCount;
      looped.assertInvocationCount(invocCount);
      myScheduler.assertScheduled();
      looped.stop();
      myScheduler.assertScheduled();
    }
  }

  public void testReschedule() throws Exception {
    final MyReactivableSingleLooped looped = new MyReactivableSingleLooped(myScheduler);
    looped.start();
    for (int i = 0; i < 5; i++) {
      myScheduler.imitateInvocation(true);
      looped.assertInvocationCount(i + 2);     // first in "start" before the cycle and i starts from 0 not 1 
      myScheduler.assertScheduled();
    }
    looped.stop();
    // must be something in a queue
    myScheduler.assertScheduled();
  }

  public void testAfterStop() throws Exception {
    final MyReactivableSingleLooped looped = new MyReactivableSingleLooped(myScheduler);
    looped.start();
    myScheduler.assertScheduled();
    looped.stop();
    myScheduler.imitateInvocation(false);
    myScheduler.assertEmpty();
    looped.assertInvocationCount(1);  // hadn't been invoked after stop
  }

  private static class MyReactivableSingleLooped extends ReactivableSingleLooped {
    private static final long SOME_TIMEOUT = 100000;

    private int myInvocationCount;
    private final MyFictiveScheduler myScheduler;

    private MyReactivableSingleLooped(MyFictiveScheduler scheduler) {
      myScheduler = scheduler;
      myInvocationCount = 0;
    }

    protected void schedule(long timeout, Runnable runnable) {
      if (DELAY == timeout) {
        // just execute to do not make fictive scheduler more complex
        runnable.run();
      } else {
        myScheduler.schedule(runnable);
      }
    }

    protected long runImpl() {
      // does not matter
      ++ myInvocationCount;
      return SOME_TIMEOUT;
    }

    protected long getDefaultTimeout() {
      return SOME_TIMEOUT;
    }

    public void reset() {
      myInvocationCount = 0;
    }

    public void assertInvocationCount(final int value) {
      Assert.assertEquals("assertInvocationCount", value, myInvocationCount);
    }
  }

  private static class MyFictiveScheduler {
    private final static long ourDefendingTimeout = 3000;
    private Runnable myActiveRunnable;

    public void schedule(final Runnable runnable) {
      Assert.assertNull("second item in a queue being scheduled!", myActiveRunnable);
      myActiveRunnable = runnable;
    }

    public void imitateInvocation(final boolean rescheduledAfter) {
      Assert.assertNotNull("nothing to invoke", myActiveRunnable);

      final Ref<Boolean> calledOk = new Ref<Boolean>();
      final Semaphore semaphore = new Semaphore();
      final Thread thread = new Thread(new Runnable() {
        public void run() {
          final Runnable runnable = myActiveRunnable;
          myActiveRunnable = null;
          runnable.run();

          Assert.assertTrue(rescheduledAfter ? (myActiveRunnable != null) : (myActiveRunnable == null));

          calledOk.set(Boolean.TRUE);
          semaphore.up();
        }
      });
      semaphore.down();
      thread.start();
      semaphore.waitFor(ourDefendingTimeout);
      
      Assert.assertTrue(Boolean.TRUE.equals(calledOk.get()));
    }

    public void assertScheduled() {
      Assert.assertNotNull("assertScheduled", myActiveRunnable);
    }

    public void assertEmpty() {
      Assert.assertNull("assertEmpty", myActiveRunnable);
    }
  }
}
