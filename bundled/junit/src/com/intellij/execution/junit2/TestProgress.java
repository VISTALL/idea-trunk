/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package com.intellij.execution.junit2;

import com.intellij.execution.junit2.ui.model.JUnitAdapter;
import com.intellij.execution.junit2.ui.model.JUnitRunningModel;
import com.intellij.execution.testframework.Filter;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.rt.execution.junit.states.PoolOfTestStates;

import javax.swing.*;

import org.jetbrains.annotations.NonNls;

public class TestProgress extends DefaultBoundedRangeModel {
  private static final Logger LOG = Logger.getInstance("#com.intellij.execution.junit2.TestProgress");
  private int myProblemsCounter = 0;
  private TestProxy myCurrentState = null;
  private final MyJUnitListener myListener = new MyJUnitListener();
  private int myMissingChildren;
  public static final Filter TEST_CASE = new Filter() {
    public boolean shouldAccept(final AbstractTestProxy test) {
      return test.shouldRun();
    }
  };

  public TestProgress() {
    super(0, 0, 0, 0);
  }

  public TestProgress(final JUnitRunningModel model) {
    this();
    setModel(model);
  }

  public void setModel(final JUnitRunningModel model) {
    myMissingChildren = 0;
    final int knownTestCases = TEST_CASE.select(model.getRoot().getAllTests()).size();
    final int declaredTestCases = model.getRoot().getInfo().getTestsCount();
    if (declaredTestCases > knownTestCases)
      myMissingChildren = declaredTestCases - knownTestCases;
    setMaximum(knownTestCases + myMissingChildren);
    model.addListener(myListener);
  }

  public int countDefects() {
    return myProblemsCounter;
  }

  public boolean hasDefects() {
    return countDefects() > 0;
  }

  public TestProxy getCurrentTest() {
    return myCurrentState;
  }

  private void setCurrentState(final TestProxy currentState) {
    myCurrentState = currentState;
    fireStateChanged();
  }

  private class MyJUnitListener extends JUnitAdapter {
    public void onTestChanged(final TestEvent event) {
      if (event instanceof StateChangedEvent)
        onChanged((StateChangedEvent) event);
      if (event instanceof NewChildEvent)
        onChildAdded((NewChildEvent) event);
    }

    public void onChanged(final StateChangedEvent event) {
      final TestProxy test = event.getSource();
      if (!test.isLeaf())
        return;
      final int stateMagnitude = test.getState().getMagnitude();
      if (test.getState().isFinal()) {
        if (!test.getInfo().shouldRun())
          newTestAppeared();
        if (getValue() >= getMaximum()) {
          @NonNls final String message = "State changed: " +test + " state: " + stateMagnitude +
                                         " Max: " + getMaximum() + " Value: "+ getValue();
          LOG.error(message);
        }

        if (test.getState().isDefect())
          myProblemsCounter++;
        setValue(getValue() + 1);
      }
      setCurrentState(stateMagnitude == PoolOfTestStates.RUNNING_INDEX ? test : null);
    }

    public void onChildAdded(final NewChildEvent event) {
      if (event.getChild().getInfo().shouldRun())
        newTestAppeared();
    }

    private void newTestAppeared() {
      if (myMissingChildren == 0)
        setMaximum(getMaximum() + 1);
      else
        myMissingChildren--;
    }
  }
}
