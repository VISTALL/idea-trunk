package com.theoryinpractice.testng.ui;

import com.intellij.execution.testframework.PoolOfTestIcons;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.theoryinpractice.testng.model.TestNGConsoleProperties;
import com.theoryinpractice.testng.model.TestNodeDescriptor;
import com.theoryinpractice.testng.model.TestProxy;
import com.theoryinpractice.testng.model.TreeRootNode;
import org.testng.remote.strprotocol.MessageHelper;
import org.testng.remote.strprotocol.TestResultMessage;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Hani Suleiman Date: Jul 21, 2005 Time: 11:32:36 PM
 */
public class ResultTreeRenderer extends ColoredTreeCellRenderer
{
    private final TestNGConsoleProperties consoleProperties;

    public ResultTreeRenderer(TestNGConsoleProperties consoleProperties) {
        this.consoleProperties = consoleProperties;
    }

    @Override
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof TestNodeDescriptor) {
            TestProxy proxy = ((TestNodeDescriptor) node.getUserObject()).getElement();
            if (node == tree.getModel().getRoot()) {
                TreeRootNode root = (TreeRootNode) proxy;
                if (node.getChildCount() == 0) {
                    if ((root.isStarted() && root.isInProgress()) || (root.isInProgress() && !root.isStarted())) {
                        setIcon(PoolOfTestIcons.NOT_RAN);
                        append("Instantiating tests... ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    } else if (root.isStarted() && !root.isInProgress()) {
                        setIcon(PoolOfTestIcons.PASSED_ICON);
                        append("All Tests Passed.", SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    } else {
                        setIcon(PoolOfTestIcons.NOT_RAN);
                        append("No Test Results.", SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    }

                } else {
                    setIcon(root.isInProgress() ? Animator.getCurrentFrame() : getIcon(proxy));
                    append(root.isInProgress() ? "Running tests..." : "Test Results", SimpleTextAttributes.REGULAR_ATTRIBUTES);
                }

                if (consoleProperties.isPaused()) {
                    setIcon(Animator.PAUSED_ICON);
                }
            } else {
                if (proxy.getResultMessage() != null) {
                    TestResultMessage result = proxy.getResultMessage();
                  final String name = node.getChildCount() == 0 ? result.toDisplayString() : result.getTestClass();
                  append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                  final String description = result.getTestDescription();
                  if (description != null && description.startsWith(name) && description.length() > name.length()) {
                    append(description.substring(name.length()), SimpleTextAttributes.GRAYED_ATTRIBUTES);
                  }
                } else {
                    append(proxy.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                }
                setIcon(proxy.isInProgress() ? Animator.getCurrentFrame() : proxy.isInterrupted() ? PoolOfTestIcons.NOT_RAN : getIcon(proxy));
            }
        } else {
            setIcon(Animator.getCurrentFrame());
            append(node.getUserObject() != null ? node.getUserObject().toString() : "null", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }

    private Icon getIcon(TestProxy node) {
        if (node.isResult()) {
            TestResultMessage result = node.getResultMessage();
            switch (result.getResult()) {
                case MessageHelper.PASSED_TEST:
                    return PoolOfTestIcons.PASSED_ICON;
                case MessageHelper.SKIPPED_TEST:
                    return PoolOfTestIcons.SKIPPED_ICON;
                case MessageHelper.FAILED_TEST:
                    return PoolOfTestIcons.FAILED_ICON;
                case MessageHelper.TEST_STARTED:
                    return PoolOfTestIcons.TERMINATED_ICON;
            }
        } else {
            boolean hasFail = false;
            boolean hasSkipped = false;
            boolean hasTerminated = false;
            for (TestProxy result : node.getChildren()) {
                Icon icon = getIcon(result);
                if (icon == PoolOfTestIcons.FAILED_ICON) {
                    hasFail = true;
                } else if (icon == PoolOfTestIcons.SKIPPED_ICON) {
                    hasSkipped = true;
                } else if (icon == PoolOfTestIcons.TERMINATED_ICON) {
                    hasTerminated = true;
                }
            }
            if (hasTerminated) return PoolOfTestIcons.TERMINATED_ICON;
            if (hasFail) return PoolOfTestIcons.FAILED_ICON;
            if (hasSkipped) return PoolOfTestIcons.SKIPPED_ICON;
        }
        return PoolOfTestIcons.PASSED_ICON;
    }
}
