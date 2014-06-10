package org.jetbrains.idea.perforce;

import org.junit.Test;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author yole
 */
public class PerforceListenerTest extends PerforceTestCase {
  @Test
  public void testCopyAddedFile() throws Exception {
    enableSilentOperation(VcsConfiguration.StandardConfirmation.ADD);
    VirtualFile a = createFileInCommand("a.txt", "a");
    copyFileInCommand(a, "b.txt");
    verifyOpened("b.txt", "add");
  }
}