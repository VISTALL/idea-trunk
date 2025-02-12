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

package com.intellij.execution.junit2.ui.model;

import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.execution.junit2.TestProgress;
import com.intellij.execution.junit2.TestProxy;
import com.intellij.execution.junit2.TestingStatus;
import com.intellij.execution.junit2.segments.DispatchListener;
import com.intellij.execution.junit2.segments.PacketExtractorBase;
import com.intellij.execution.junit2.ui.Animator;
import com.intellij.execution.junit2.ui.TestProxyClient;
import com.intellij.execution.junit2.ui.properties.JUnitConsoleProperties;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.Filter;
import com.intellij.execution.testframework.TestFrameworkRunningModel;
import com.intellij.execution.testframework.TestTreeView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class JUnitRunningModel implements TestFrameworkRunningModel {
  private final TestProgress myProgress;
  private final TestProxy myRoot;
  private final TestingStatus myStatus;
  private final JUnitConsoleProperties myProperties;
  private final MyTreeSelectionListener myTreeListener = new MyTreeSelectionListener();
  private JTree myTreeView;
  private TestTreeBuilder myTreeBuilder;

  private final JUnitListenersNotifier myNotifier = new JUnitListenersNotifier();
  private final Animator myAnimator;
  private PacketExtractorBase myPacketExtractor;

  public JUnitRunningModel(final TestProxy root, final TestingStatus status, final JUnitConsoleProperties properties) {
    myRoot = root;
    myStatus = status;
    myProperties = properties;

    myRoot.setEventsConsumer(myNotifier);

    myProgress = new TestProgress(this);
    myStatus.setListener(myNotifier);

    Disposer.register(this, myTreeListener);
    Disposer.register(this, new Disposable() {
      public void dispose() {
        myNotifier.onDispose(JUnitRunningModel.this);
      }
    });
    myAnimator = new Animator(this);
  }

  public TestTreeBuilder getTreeBuilder() {
    return myTreeBuilder;
  }

  public TestingStatus getStatus() { return myStatus; }

  public void attachToTree(final TestTreeView treeView) {
    myTreeBuilder = new TestTreeBuilder(treeView, this, myProperties);
    Disposer.register(this, myTreeBuilder);
    myAnimator.setModel(this);
    myTreeView = treeView;
    selectTest(getRoot());
    myTreeListener.install();
  }

  public void dispose() {
  }

  public TestTreeView getTreeView() {
    return (TestTreeView) myTreeBuilder.getTree();
  }

  public boolean hasTestSuites() {
    return myRoot.hasChildSuites();
  }

  public TestProgress getProgress() {
    return myProgress;
  }

  public TestProxy getRoot() {
    return myRoot;
  }

  public void selectAndNotify(final AbstractTestProxy testProxy) {
    selectTest((TestProxy)testProxy);
    myNotifier.onTestSelected((TestProxy)testProxy);
  }

  public Project getProject() {
    return myProperties.getProject();
  }

  public JUnitConsoleProperties getProperties() { return myProperties; }

  public void selectTest(final TestProxy test) {
    if (test == null) return;

    myTreeBuilder.select(test, null);
  }


  public void collapse(final TestProxy test) {
    if (test == getRoot())
      return;
    final TreePath path = pathToTest(test, false);
    if (path == null) return;
    myTreeView.collapsePath(path);
  }

  public DispatchListener getNotifier() {
    return myNotifier;
  }

  public void setFilter(final Filter filter) {
    final TestTreeStructure treeStructure = getStructure();
    treeStructure.setFilter(filter);
    myTreeBuilder.updateFromRoot();
  }

  public boolean isRunning() {
    return getStatus().isRunning();
  }

  private TestTreeStructure getStructure() {
    return (TestTreeStructure)myTreeBuilder.getTreeStructure();
  }

  public void addListener(final JUnitListener listener) {
    myNotifier.addListener(listener);
  }

  public void removeListener(final JUnitListener listener) {
    myNotifier.removeListener(listener);
  }

  public void onUIBuilt() {
    myNotifier.onTestSelected(myRoot);
  }

  private TreePath pathToTest(final TestProxy test, final boolean expandIfCollapsed) {
    final TestTreeBuilder treeBuilder = getTreeBuilder();
    DefaultMutableTreeNode node = treeBuilder.getNodeForElement(test);
    if (node == null && !expandIfCollapsed)
      return null;
    node = treeBuilder.ensureTestVisible(test);
    if (node == null)
      return null;
    return TreeUtil.getPath((TreeNode) myTreeView.getModel().getRoot(), node);
  }

  public boolean hasInTree(final AbstractTestProxy test) {
    return getStructure().getFilter().shouldAccept(test);
  }

  public void attachTo(final PacketExtractorBase packetExtractor) {
    myPacketExtractor = packetExtractor;
    myPacketExtractor.setDispatchListener(myNotifier);
    Disposer.register(this, new Disposable() {
      public void dispose() {
        myPacketExtractor.setDispatchListener(DispatchListener.DEAF);
      }
    });
  }

  public JUnitConfiguration getConfiguration() {
    return myProperties.getConfiguration();
  }

  private class MyTreeSelectionListener extends FocusAdapter implements TreeSelectionListener, Disposable {

    public void valueChanged(final TreeSelectionEvent e) {
      final TestProxy test = TestProxyClient.from(e.getPath());
      if (myTreeView.isFocusOwner())
        myNotifier.onTestSelected(test);
    }

    public void focusGained(final FocusEvent e) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              if (!myTreeBuilder.isDisposed()) {
                myNotifier.onTestSelected((TestProxy)getTreeView().getSelectedTest());
              }
            }
          });
    }

    public void install() {
      myTreeView.addTreeSelectionListener(this);
      myTreeView.addFocusListener(this);
    }

    public void dispose() {
      if (myTreeView != null) {
        myTreeView.removeTreeSelectionListener(this);
        myTreeView.removeFocusListener(this);
      }
    }
  }
}
