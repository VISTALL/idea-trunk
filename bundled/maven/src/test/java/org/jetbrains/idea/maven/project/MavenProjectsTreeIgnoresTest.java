package org.jetbrains.idea.maven.project;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;

import java.util.Collections;
import java.util.List;

public class MavenProjectsTreeIgnoresTest extends MavenProjectsTreeTestCase {
  private String myLog = "";
  private List<MavenProject> myRoots;

  @Override
  protected void setUpInWriteAction() throws Exception {
    super.setUpInWriteAction();
    myTree.addListener(new MyLoggingListener());
    VirtualFile m1 = createModulePom("m1",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m1</artifactId>" +
                                     "<version>1</version>");

    VirtualFile m2 = createModulePom("m2",
                                     "<groupId>test</groupId>" +
                                     "<artifactId>m2</artifactId>" +
                                     "<version>1</version>");
    updateAll(m1, m2);
    myRoots = myTree.getRootProjects();
  }

  public void testSendingNotifications() throws Exception {
    myTree.setIgnoredState(Collections.singletonList(myRoots.get(0)), true);

    assertEquals("ignored: m1 ", myLog);
    myLog = "";

    myTree.setIgnoredFilesPaths(Collections.singletonList(myRoots.get(1).getPath()));

    assertEquals("ignored: m2 unignored: m1 ", myLog);
    myLog = "";

    myTree.setIgnoredFilesPatterns(Collections.singletonList("*"));

    assertEquals("ignored: m1 ", myLog);
    myLog = "";

    myTree.setIgnoredFilesPatterns(Collections.EMPTY_LIST);

    assertEquals("unignored: m1 ", myLog);
    myLog = "";
  }

  public void testDoNotSendNotificationsIfNothingChanged() throws Exception {
    myTree.setIgnoredState(Collections.singletonList(myRoots.get(0)), true);

    assertEquals("ignored: m1 ", myLog);
    myLog = "";

    myTree.setIgnoredState(Collections.singletonList(myRoots.get(0)), true);

    assertEquals("", myLog);
  }

  private class MyLoggingListener extends MavenProjectsTree.ListenerAdapter {
    @Override
    public void projectsIgnoredStateChanged(List<MavenProject> ignored, List<MavenProject> unignored, Object message) {
      if (!ignored.isEmpty()) myLog += "ignored: " + format(ignored) + " ";
      if (!unignored.isEmpty()) myLog += "unignored: " + format(unignored) + " ";
      if (ignored.isEmpty() && unignored.isEmpty()) myLog += "empty ";
    }

    private String format(List<MavenProject> projects) {
      return StringUtil.join(projects, new Function<MavenProject, String>() {
        public String fun(MavenProject project) {
          return project.getMavenId().getArtifactId();
        }
      }, ", ");
    }
  }
}