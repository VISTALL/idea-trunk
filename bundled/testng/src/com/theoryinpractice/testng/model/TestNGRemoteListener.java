/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: Jul 11, 2005
 * Time: 9:02:27 PM
 */
package com.theoryinpractice.testng.model;

import com.theoryinpractice.testng.ui.TestNGConsoleView;
import com.theoryinpractice.testng.ui.TestNGResults;
import org.testng.remote.strprotocol.*;

public class TestNGRemoteListener implements IRemoteSuiteListener, IRemoteTestListener {
    private final TestNGConsoleView console;

    public TestNGRemoteListener(TestNGConsoleView console) {
        this.console = console;
    }

    public void onInitialization(GenericMessage genericMessage) {
    }

    public void onStart(SuiteMessage suiteMessage) {
      final TestNGResults view = console.getResultsView();
      if (view != null) {
        view.start();
      }
    }

    public void onFinish(SuiteMessage suiteMessage) {
      console.flush();
      final TestNGResults view = console.getResultsView();
      if (view != null) {
        view.finish();
      }
    }

    public void onStart(TestMessage tm) {
      final TestNGResults view = console.getResultsView();
      if (view != null) {
        view.setTotal(tm.getTestMethodCount());
      }
    }

    public void onTestStart(TestResultMessage trm) {
        console.testStarted(trm);
    }

    public void onFinish(TestMessage tm) {
        console.rebuildTree();
    }

    public void onTestSuccess(TestResultMessage trm) {
        console.addTestResult(trm);
    }

    public void onTestFailure(TestResultMessage trm) {
        console.addTestResult(trm);
    }

    public void onTestSkipped(TestResultMessage trm) {
        console.addTestResult(trm);
    }

    public void onTestFailedButWithinSuccessPercentage(TestResultMessage trm) {
    }
}
