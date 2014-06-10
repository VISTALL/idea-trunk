package org.jetbrains.plugins.groovy.mvc;

import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.AsyncProcessIcon;

import javax.swing.*;
import java.awt.*;

/**
 * User: Dmitry.Krasilschikov
* Date: 09.09.2008
*/
public class MvcLoadingPanel extends JPanel {
  private EmptyIcon myEmptyIcon;
  private final AsyncProcessIcon myRefreshIcon = new AsyncProcessIcon("Refreshing filesystem") {
    @Override
    protected Icon getPassiveIcon() {
      myEmptyIcon = new EmptyIcon(myRefreshIcon.getPreferredSize().width, myRefreshIcon.getPreferredSize().height);
      return myEmptyIcon;
    }
  };

  public MvcLoadingPanel() {
    setLayout(new BorderLayout());
    add(myRefreshIcon, BorderLayout.LINE_START);
    myRefreshIcon.suspend();
  }

  public AsyncProcessIcon getRefreshIcon() {
    return myRefreshIcon;
  }
}
