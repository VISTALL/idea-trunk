package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.PerforceContentRevision;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
@State(
  name="LastSuccessfulUpdateTracker",
  storages= {
    @Storage(
      id="other",
      file = "$WORKSPACE_FILE$"
    )}
)
public class LastSuccessfulUpdateTracker implements PersistentStateComponent<LastSuccessfulUpdateTracker.ChangesUpdateResult>, Disposable {
  public static LastSuccessfulUpdateTracker getInstance(Project project) {
    return ServiceManager.getService(project, LastSuccessfulUpdateTracker.class);
  }

  public static class ChangedFile {
    @Attribute("beforePath")
    public String beforePath;
    @Attribute("afterPath")   
    public String afterPath;
    @Attribute("beforeRevision")
    public long beforeRevision = -1;
  }

  public static class PersistentChangeList {
    public String name;
    public List<ChangedFile> files = new ArrayList<ChangedFile>();
  }

  public static class ChangesUpdateResult {
    public List<PersistentChangeList> changeLists = new ArrayList<PersistentChangeList>();
  }

  private final Project myProject;
  private final ChangeListManager myChangeListManager;
  private ChangesUpdateResult myResult = new ChangesUpdateResult();
  private boolean myUpdateSuccessful;
  private final MyChangeListListener myChangeListListener;

  public LastSuccessfulUpdateTracker(Project project, ChangeListManager changeListManager) {
    myProject = project;
    myChangeListManager = changeListManager;
    myChangeListListener = new MyChangeListListener();
    changeListManager.addChangeListListener(myChangeListListener);
  }

  public void dispose() {
    myChangeListManager.removeChangeListListener(myChangeListListener);
  }

  public ChangesUpdateResult getState() {
    return myResult;
  }

  public void loadState(ChangesUpdateResult state) {
    myResult = state;
  }

  public void updateStarted() {
    myUpdateSuccessful = false;
  }

  public void updateSuccessful() {
    myUpdateSuccessful = true;
  }

  public List<PersistentChangeList> getChangeLists() {
    return myResult.changeLists;
  }

  private class MyChangeListListener extends ChangeListAdapter {
    public void changeListUpdateDone() {
      if (myUpdateSuccessful && PerforceSettings.getSettings(myProject).ENABLED) {
        List<PersistentChangeList> results = new ArrayList<PersistentChangeList>();
        List<LocalChangeList> changeLists = myChangeListManager.getChangeLists();
        for(LocalChangeList changeList: changeLists) {
          PersistentChangeList persistentList = new PersistentChangeList();
          persistentList.name = changeList.getName();
          persistentList.files = new ArrayList<ChangedFile>();
          results.add(persistentList);

          for(Change c: changeList.getChanges()) {
            ChangedFile f = new ChangedFile();
            ContentRevision beforeRevision = c.getBeforeRevision();
            if (beforeRevision != null) {
              f.beforePath = beforeRevision.getFile().getPath();
              if (beforeRevision instanceof PerforceContentRevision) {
                PerforceContentRevision pcr = (PerforceContentRevision) beforeRevision;
                f.beforeRevision = pcr.getRevision();
              }
            }
            ContentRevision afterRevision = c.getAfterRevision();
            f.afterPath = afterRevision == null ? null : afterRevision.getFile().getPath();
            persistentList.files.add(f);
          }
        }
        myResult.changeLists = results;
      }
    }
  }
}
