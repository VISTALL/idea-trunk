/*
 * @author max
 */
package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.idea.perforce.PerforceBundle;

import javax.swing.*;

public class StallConnectionUtil {
  private StallConnectionUtil() {
  }

  public static int requestUser() {
    return Messages.showDialog(PerforceBundle.message("confirmation.text.perforce.server.not.responding.disable.integration"),
                               PerforceBundle.message("dialog.title.perforce"),
                               new String[]{
                                 PerforceBundle.message("button.text.wait.more"),
                                 PerforceBundle.message("button.text.resent.and.disable.integration")
                               },
                               0,
                               Messages.getQuestionIcon());
  }

  static boolean needDialog() {
    return SwingUtilities.isEventDispatchThread() || ProgressManager.getInstance().hasModalProgressIndicator();
  }
}