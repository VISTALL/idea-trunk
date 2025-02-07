package org.jetbrains.idea.maven.tasks;

import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.KeymapManagerListener;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.MavenMergingUpdateQueue;
import org.jetbrains.idea.maven.utils.MavenUtil;
import org.jetbrains.idea.maven.utils.SimpleProjectComponent;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MavenShortcutsManager extends SimpleProjectComponent {
  private static final String ACTION_ID_PREFIX = "Maven_";

  private final AtomicBoolean isInitialized = new AtomicBoolean();

  private final MavenProjectsManager myProjectsManager;

  private MyKeymapListener myKeymapListener;
  private final List<Listener> myListeners = ContainerUtil.createEmptyCOWList();

  public static MavenShortcutsManager getInstance(Project project) {
    return project.getComponent(MavenShortcutsManager.class);
  }

  public MavenShortcutsManager(Project project, MavenProjectsManager projectsManager, MavenRunner runner) {
    super(project);
    myProjectsManager = projectsManager;
  }

  @Override
  public void initComponent() {
    if (!isNormalProject()) return;

    MavenUtil.runWhenInitialized(myProject, new DumbAwareRunnable() {
      public void run() {
        doInit();
      }
    });
  }

  @TestOnly
  public void doInit() {
    if (isInitialized.getAndSet(true)) return;

    MyProjectsTreeListener listener = new MyProjectsTreeListener();
    myProjectsManager.addManagerListener(listener);
    myProjectsManager.addProjectsTreeListener(listener);

    myKeymapListener = new MyKeymapListener();
  }

  @Override
  public void disposeComponent() {
    if (!isInitialized.getAndSet(false)) return;

    myKeymapListener.stopListen();
    MavenKeymapExtension.clearActions(myProject);
  }

  public String getActionId(@Nullable String projectPath, @Nullable String goal) {
    StringBuilder result = new StringBuilder(ACTION_ID_PREFIX);
    result.append(myProject.getLocationHash());

    if (projectPath != null) {
      String portablePath = FileUtil.toSystemIndependentName(projectPath);

      result.append(new File(portablePath).getParentFile().getName());
      result.append(Integer.toHexString(portablePath.hashCode()));

      if (goal != null) result.append(goal);
    }

    return result.toString();
  }

  public String getDescription(MavenProject project, String goal) {
    String actionId = getActionId(project.getPath(), goal);
    if (actionId == null) return "";

    Keymap activeKeymap = KeymapManager.getInstance().getActiveKeymap();
    Shortcut[] shortcuts = activeKeymap.getShortcuts(actionId);
    if (shortcuts == null || shortcuts.length == 0) return "";

    return KeymapUtil.getShortcutsText(shortcuts);
  }

  private void fireShortcutsUpdated() {
    for (Listener listener : myListeners) {
      listener.shortcutsUpdated();
    }
  }

  public void addListener(Listener listener) {
    myListeners.add(listener);
  }

  public interface Listener {
    void shortcutsUpdated();
  }

  private class MyKeymapListener implements KeymapManagerListener, Keymap.Listener {
    private Keymap myCurrentKeymap = null;

    public MyKeymapListener() {
      KeymapManager keymapManager = KeymapManager.getInstance();
      listenTo(keymapManager.getActiveKeymap());
      keymapManager.addKeymapManagerListener(this);
    }

    public void activeKeymapChanged(Keymap keymap) {
      listenTo(keymap);
      fireShortcutsUpdated();
    }

    private void listenTo(Keymap keymap) {
      if (myCurrentKeymap != null) {
        myCurrentKeymap.removeShortcutChangeListener(this);
      }
      myCurrentKeymap = keymap;
      if (myCurrentKeymap != null) {
        myCurrentKeymap.addShortcutChangeListener(this);
      }
    }

    public void onShortcutChanged(String actionId) {
      fireShortcutsUpdated();
    }

    public void stopListen() {
      listenTo(null);
      KeymapManagerEx.getInstanceEx().removeKeymapManagerListener(this);
    }
  }

  private class MyProjectsTreeListener extends MavenProjectsTree.ListenerAdapter implements MavenProjectsManager.Listener {
    private final Map<MavenProject, Boolean> mySheduledProjects = new THashMap<MavenProject, Boolean>();
    private final MergingUpdateQueue myUpdateQueue = new MavenMergingUpdateQueue(getComponentName() + ": Keymap Update",
                                                                                 500, true, myProject);

    public void activated() {
      scheduleKeymapUpdate(myProjectsManager.getNonIgnoredProjects(), true);
    }

    public void scheduledImportsChanged() {
    }

    @Override
    public void projectsIgnoredStateChanged(List<MavenProject> ignored, List<MavenProject> unignored, Object message) {
      scheduleKeymapUpdate(unignored, true);
      scheduleKeymapUpdate(ignored, false);
    }

    @Override
    public void projectsUpdated(List<Pair<MavenProject, MavenProjectChanges>> updated, List<MavenProject> deleted, Object message) {
      scheduleKeymapUpdate(MavenUtil.collectFirsts(updated), true);
      scheduleKeymapUpdate(deleted, false);
    }

    @Override
    public void projectResolved(Pair<MavenProject, MavenProjectChanges> projectWithChanges,
                                org.apache.maven.project.MavenProject nativeMavenProject,
                                Object message) {
      scheduleKeymapUpdate(Collections.singletonList(projectWithChanges.first), true);
    }

    @Override
    public void pluginsResolved(MavenProject project) {
      scheduleKeymapUpdate(Collections.singletonList(project), true);
    }

    private void scheduleKeymapUpdate(List<MavenProject> mavenProjects, boolean forUpdate) {
      synchronized (mySheduledProjects) {
        for (MavenProject each : mavenProjects) {
          mySheduledProjects.put(each, forUpdate);
        }
      }

      myUpdateQueue.queue(new Update(MavenShortcutsManager.this) {
        public void run() {
          List<MavenProject> projectToUpdate;
          List<MavenProject> projectToDelete;
          synchronized (mySheduledProjects) {
            projectToUpdate = selectScheduledProjects(true);
            projectToDelete = selectScheduledProjects(false);
            mySheduledProjects.clear();
          }
          MavenKeymapExtension.clearActions(myProject, projectToDelete);
          MavenKeymapExtension.updateActions(myProject, projectToUpdate);
        }
      });
    }

    private List<MavenProject> selectScheduledProjects(final boolean forUpdate) {
      return ContainerUtil.mapNotNull(mySheduledProjects.entrySet(), new Function<Map.Entry<MavenProject, Boolean>, MavenProject>() {
        public MavenProject fun(Map.Entry<MavenProject, Boolean> eachEntry) {
          return forUpdate == eachEntry.getValue() ? eachEntry.getKey() : null;
        }
      });
    }
  }
}
