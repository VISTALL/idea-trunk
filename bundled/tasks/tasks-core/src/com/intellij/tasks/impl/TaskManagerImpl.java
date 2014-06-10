package com.intellij.tasks.impl;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.tasks.*;
import com.intellij.tasks.context.WorkingContextManager;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.ui.ColoredTreeCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.*;


/**
 * @author Dmitry Avdeev
 */
@State(
    name = "TaskManager",
    storages = {@Storage(id = "default", file = "$WORKSPACE_FILE$")})
public class TaskManagerImpl extends TaskManager implements ProjectComponent, PersistentStateComponent<TaskManagerImpl.Config>,
                                                            ChangeListDecorator {

  private static final Logger LOG = Logger.getInstance("#com.intellij.tasks.impl.TaskManagerImpl");

  private static final DecimalFormat LOCAL_TASK_ID_FORMAT = new DecimalFormat("LOCAL-00000");
  public static final Comparator<Task> TASK_UPDATE_COMPARATOR = new Comparator<Task>() {
    public int compare(Task o1, Task o2) {
      int i = Comparing.compare(o2.getUpdated(), o1.getUpdated());
      return i == 0 ? Comparing.compare(o2.getCreated(), o1.getCreated()) : i;
    }
  };

  private final Project myProject;

  private final WorkingContextManager myContextManager;

  private final Map<String,Task> myIssueCache = Collections.synchronizedMap(new HashMap<String,Task>());

  private final LinkedHashMap<String, LocalTaskImpl> myTasks = new LinkedHashMap<String, LocalTaskImpl>() {
    @Override
    public LocalTaskImpl put(String key, LocalTaskImpl task) {
      LocalTaskImpl result = super.put(key, task);
      if (size() > myConfig.taskHistoryLength) {
        ArrayList<LocalTask> list = new ArrayList<LocalTask>(values());
        Collections.sort(list, TASK_UPDATE_COMPARATOR);
        for (LocalTask oldest : list) {
          if (!oldest.isDefault()) {
            remove(oldest);
            break;
          }
        }
      }
      return result;
    }
  };
  @NotNull
  private LocalTask myActiveTask = createDefaultTask();
  private Timer myCacheRefreshTimer;

  private volatile boolean myUpdating;
  private final Config myConfig = new Config();
  private final ChangeListAdapter myChangeListListener;
  private final ChangeListManager myChangeListManager;
  @NonNls private static final String DEFAULT_TASK_ID = "Default";

  public TaskManagerImpl(Project project, WorkingContextManager contextManager, ChangeListManager changeListManager) {

    myProject = project;
    myContextManager = contextManager;
    myChangeListManager = changeListManager;

    myChangeListListener = new ChangeListAdapter() {
      @Override
      public void changeListAdded(ChangeList list) {
        getOpenChangelists(myActiveTask).add(new ChangeListInfo((LocalChangeList)list));
      }

      @Override
      public void changeListRemoved(ChangeList list) {
        getOpenChangelists(myActiveTask).remove(new ChangeListInfo((LocalChangeList)list));
      }

      @Override
      public void defaultListChanged(ChangeList oldDefaultList, ChangeList newDefaultList) {

        final LocalTask associatedTask = getAssociatedTask((LocalChangeList)newDefaultList);
        if (associatedTask != null) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              associatedTask.setUpdated(new Date());
              activateTask(associatedTask, true, false);              
            }
          });
          return;
        }

        final LocalTask task = findTaskByChangelist(newDefaultList);
        if (task != null) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              saveActiveTask();
              doActivate(task, true);
            }
          }, myProject.getDisposed());
        }
      }
    };
  }

  @Nullable
  private LocalTask findTaskByChangelist(ChangeList newDefaultList) {
    ChangeListInfo info = new ChangeListInfo((LocalChangeList)newDefaultList);
    for (final LocalTask task : myTasks.values()) {
      if (((LocalTaskImpl)task).getChangeLists().contains(info)) {
        return task;
      }
    }
    return null;
  }

  @Override
  public TaskRepository[] getAllRepositories() {
    TaskRepositoryType[] types = getAllRepositoryTypes();
    ArrayList<TaskRepository> repositories = new ArrayList<TaskRepository>();
    for (TaskRepositoryType group : types) {
      repositories.addAll(Arrays.asList(group.getRepositories()));
    }
    return repositories.toArray(new TaskRepository[repositories.size()]);
  }

  @Override
  public void configureRepositories(TaskRepository[] newRepositories) {
    for (final TaskRepositoryType repositoryType : getAllRepositoryTypes()) {
      List<TaskRepository> list = ContainerUtil.findAll(newRepositories, new Condition<TaskRepository>() {
        public boolean value(TaskRepository repository) {
          return repositoryType == repository.getType();
        }
      });
      repositoryType.configureRepositories(list.toArray(new TaskRepository[list.size()]));
    }
  }

  public TaskRepositoryType[] getAllRepositoryTypes() {
    return Extensions.getExtensions(TaskRepositoryType.EP_NAME, myProject);
  }

  @Override
  public void closeTask(LocalTask task) {
    
  }

  @Override
  public void removeTask(LocalTask task) {
    myTasks.remove(task.getId());
    myContextManager.removeContext(task);
  }

  @NotNull
  @Override
  public LocalTask getActiveTask() {
    return myActiveTask;
  }

  @Override
  public Map<String, Task> getCachedIssues() {
    return myIssueCache;
  }

  @Nullable
  @Override
  public Task updateIssue(String id) {
    for (TaskRepository repository : getAllRepositories()) {
      if (repository.extractId(id) != null) {
        Task issue = repository.findTask(id);
        if (issue != null) {
          LocalTaskImpl localTask = myTasks.get(id);
          if (localTask != null) {
            localTask.updateFromIssue(issue);
            return localTask;
          }
          return issue;
        }
      }
    }
    return null;
  }

  @Override
  public LocalTask[] getLocalTasks() {
    synchronized (myTasks) {
      return myTasks.values().toArray(new LocalTask[myTasks.size()]);
    }
  }

  @Override
  public LocalTaskImpl createLocalTask(String summary) {
    return createTask(LOCAL_TASK_ID_FORMAT.format(myConfig.localTasksCounter++), summary);
  }

  private static LocalTaskImpl createTask(String id, String summary) {
    LocalTaskImpl task = new LocalTaskImpl(id, summary);
    Date date = new Date();
    task.setCreated(date);
    task.setUpdated(date);
    return task;
  }

  @Override
  public void activateTask(@NotNull Task task, boolean clearContext, boolean createChangelist) {
    
    myConfig.clearContext = clearContext;

    saveActiveTask();

    if (clearContext) {
      myContextManager.clearContext();
    }
    myContextManager.restoreContext(task);

    task = doActivate(task, true);
    
    List<ChangeListInfo> changeLists = getOpenChangelists(task);
    if (changeLists.isEmpty()) {
      myConfig.createChangelist = createChangelist;
    }

    if (createChangelist) {
      if (changeLists.isEmpty()) {
        String name = TaskUtil.getChangeListName(task);
        createChangeList(task, name);
      } else {
        String id = changeLists.get(0).id;
        LocalChangeList changeList = myChangeListManager.getChangeList(id);
        if (changeList != null) {
          myChangeListManager.setDefaultChangeList(changeList);
        }
      }
    }
  }

  private void saveActiveTask() {
    myContextManager.saveContext(myActiveTask);
    myActiveTask.setActive(false);
    myActiveTask.setUpdated(new Date());
  }

  public void createChangeList(Task task, String name) {
    LocalChangeList changeList = myChangeListManager.findChangeList(name);
    if (changeList == null) {
      changeList = myChangeListManager.addChangeList(name, null);
    }
    myChangeListManager.setDefaultChangeList(changeList);
    getOpenChangelists(task).add(new ChangeListInfo(changeList));
  }

  private LocalTask doActivate(Task origin, boolean explicitly) {
    LocalTaskImpl task = origin instanceof LocalTaskImpl ? (LocalTaskImpl)origin : new LocalTaskImpl(origin);
    if (explicitly) {
      task.setUpdated(new Date());
    }
    task.setActive(true);
    myTasks.put(task.getId(), task);
    updateIssue(task.getId());
    myActiveTask = task;
    return task;
  }

  @Override
  public boolean testConnection(final TaskRepository repository) {

    MyProgressTask task = new MyProgressTask("Test connection") {
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setText("Connecting...");
        indicator.setFraction(0);
        indicator.setIndeterminate(true);
        try {
          myThread = Thread.currentThread();
          repository.testConnection();
        }
        catch (Exception e) {
          LOG.info(e);
          myException = e;
        }
      }
    };
    ProgressManager.getInstance().run(task);
    Exception exception = task.myException;
    if (exception == null) {
      Messages.showMessageDialog(myProject, "Connection is successful", "Connection", Messages.getInformationIcon());
    } else {
      Messages.showErrorDialog(myProject, exception.getMessage(), "Error");
    }
    return exception == null;
  }

  public Config getState() {
    myConfig.tasks = ContainerUtil.map(myTasks.values(), new Function<Task, LocalTaskImpl>() {
      public LocalTaskImpl fun(Task task) {
        return new LocalTaskImpl(task);
      }
    });
    return myConfig;
  }

  public void loadState(Config config) {
    XmlSerializerUtil.copyBean(config, myConfig);
    myTasks.clear();
    for (LocalTaskImpl task : config.tasks) {
      if (!task.isClosed()) {
        myTasks.put(task.getId(), task);
      }
    }

    LocalTaskImpl activeTask = null;
    Collections.sort(config.tasks, TASK_UPDATE_COMPARATOR);
    for (LocalTaskImpl task : config.tasks) {
      if (activeTask == null) {
        if (task.isActive()) {
          activeTask = task;
        }
      } else {
        task.setActive(false);
      }
    }

    if (activeTask != null) {
      myActiveTask = activeTask;
    }
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  @NotNull
  public String getComponentName() {
    return "Task Manager";
  }

  public void initComponent() {
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      myCacheRefreshTimer = new Timer(myConfig.updateInterval*60*1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (myConfig.updateEnabled && !myUpdating) {
            updateIssues();
          }
        }
      });
      myCacheRefreshTimer.setInitialDelay(0);

      StartupManager.getInstance(myProject).registerStartupActivity(new Runnable() {
        public void run() {
          myCacheRefreshTimer.start();
        }
      });
    }

    LocalTaskImpl defaultTask = myTasks.get(DEFAULT_TASK_ID);
    if (defaultTask == null) {
      defaultTask = createDefaultTask();
      myTasks.put(DEFAULT_TASK_ID, defaultTask);
    }
    // make sure the task is associated with default changelist
    LocalChangeList defaultList = myChangeListManager.findChangeList(LocalChangeList.DEFAULT_NAME);
    if (defaultList != null) {
      ChangeListInfo listInfo = new ChangeListInfo(defaultList);
      if (!defaultTask.getChangeLists().contains(listInfo)) {
        defaultTask.getChangeLists().add(listInfo);
      }
    }

    // update tasks from change lists
    HashSet<ChangeListInfo> infos = new HashSet<ChangeListInfo>();
    for (LocalTask task : myTasks.values()) {
      infos.addAll(((LocalTaskImpl)task).getChangeLists());
    }
    List<LocalChangeList> changeLists = myChangeListManager.getChangeLists();
    for (LocalChangeList localChangeList : changeLists) {
      ChangeListInfo info = new ChangeListInfo(localChangeList);
      if (!infos.contains(info)) {
        String name = localChangeList.getName();
        String id = extractId(name);
        LocalTask existing = id == null ? myTasks.get(name) : myTasks.get(id);
        if (existing != null) {
          ((LocalTaskImpl)existing).getChangeLists().add(info);
        } else {
          LocalTaskImpl task;
          if (id == null) {
            task = createLocalTask(name);
          }
          else {
            task = createTask(id, name);
            task.setIssue(true);
          }
          task.getChangeLists().add(info);          
          myTasks.put(task.getId(), task);
        }
      }
    }

    doActivate(myActiveTask, false);

    myChangeListManager.addChangeListListener(myChangeListListener);
  }

  private static LocalTaskImpl createDefaultTask() {
    return new LocalTaskImpl(DEFAULT_TASK_ID, "Default task") {
      @Override
      public boolean isDefault() {
        return true;
      }
    };
  }

  @Nullable
  private String extractId(String text) {
    for (TaskRepository repository : getAllRepositories()) {
      String id = repository.extractId(text);
      if (id != null) {
        return id;
      }
    }
    return null;
  }

  public void disposeComponent() {
    if (myCacheRefreshTimer != null) {
      myCacheRefreshTimer.stop();
    }
    ChangeListManager.getInstance(myProject).removeChangeListListener(myChangeListListener);
  }

  public void updateIssues() {
    TaskRepository first = ContainerUtil.find(getAllRepositories(), new Condition<TaskRepository>() {
      public boolean value(TaskRepository repository) {
        return repository.isConfigured();
      }
    });
    if (first == null) {
      return;
    }
    myUpdating = true;
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        try {
          for (TaskRepository repository : getAllRepositories()) {
            if (repository.isConfigured()) {
              Task[] issues = repository.getMyIssues(myConfig.updateIssuesCount);
              for (Task issue : issues) {
                myIssueCache.put(issue.getId(), issue);
              }

              // update local tasks
              synchronized (myTasks) {
                for (Iterator<Map.Entry<String,LocalTaskImpl>> it = myTasks.entrySet().iterator(); it.hasNext();) {
                  Map.Entry<String,LocalTaskImpl> entry = it.next();
                  Task issue = myIssueCache.get(entry.getKey());
                  if (issue != null) {
                    if (issue.isClosed()) {
                      it.remove();
                    } else {
                      entry.getValue().updateFromIssue(issue);
                    }
                  }
                }
              }
            }
          }
        }
        finally {
          myUpdating = false;
        }
      }
    });
  }

  @Override
  public boolean isVcsEnabled() {
    return ProjectLevelVcsManager.getInstance(myProject).getAllActiveVcss().length > 0;
  }

  @Nullable
  @Override
  public LocalTask getAssociatedTask(LocalChangeList list) {
    for (LocalTaskImpl task : myTasks.values()) {
      if (list.getId().equals(task.getAssociatedChangelistId())) {
        return task;
      }
    }
    return null;
  }

  @Override
  public void associateWithTask(LocalChangeList changeList) {
    String id = changeList.getId();
    for (LocalTaskImpl localTask : myTasks.values()) {
      if (localTask.getAssociatedChangelistId() == null) {
        for (ChangeListInfo info : localTask.getChangeLists()) {
          if (id.equals(info.id)) {
            localTask.setAssociatedChangelistId(id);
            return;
          }
        }
      }
    }
    String comment = changeList.getComment();
    LocalTaskImpl task = createLocalTask(StringUtil.isEmpty(comment) ? changeList.getName() : comment);
    task.getChangeLists().add(new ChangeListInfo(changeList));
    task.setAssociatedChangelistId(id);
  }

  @Override
  public List<ChangeListInfo> getOpenChangelists(Task task) {
    if (task instanceof LocalTaskImpl) {
      List<ChangeListInfo> changeLists = ((LocalTaskImpl)task).getChangeLists();
      for (Iterator<ChangeListInfo> it = changeLists.iterator(); it.hasNext();) {
        ChangeListInfo changeList = it.next();
        if (myChangeListManager.getChangeList(changeList.id) == null) {
          it.remove();
        }
      }
      return changeLists;
    }
    else {
      return Collections.emptyList();
    }
  }

  public void decorateChangeList(LocalChangeList changeList, ColoredTreeCellRenderer cellRenderer, boolean selected,
                                 boolean expanded, boolean hasFocus) {
    LocalTask task = getAssociatedTask(changeList);
    if (task == null) {
      task = findTaskByChangelist(changeList);
    }
    if (task != null && task.isIssue()) {
      cellRenderer.setIcon(task.getIcon());
    }
  }

  public static class Config {    
    public List<LocalTaskImpl> tasks = new ArrayList<LocalTaskImpl>();
    public int localTasksCounter = 1;

    public int taskHistoryLength = 50;

    public boolean updateEnabled = true;
    public int updateInterval = 5;
    public int updateIssuesCount = 50;

    public boolean clearContext = true;
    public boolean createChangelist = true;
    public boolean trackContextForNewChangelist = true;
  }

  private abstract class MyProgressTask extends com.intellij.openapi.progress.Task.Modal {

    protected Thread myThread;
    protected Exception myException;

    public MyProgressTask(String title) {
      super(TaskManagerImpl.this.myProject, title, true);
    }

    @Override
    public void onCancel() {
      if (myThread != null) {
        myThread.interrupt();
      }
    }
  }
}
