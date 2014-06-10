package com.intellij.tasks.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Dmitry Avdeev
 */
@Tag("server")
public class JiraRepository extends JiraServerCfg implements TaskRepository {

  private final static Logger LOG = Logger.getInstance("#com.intellij.tasks.jira.JiraRepository");
  private TaskRepositoryType myType;
  private static final Pattern JIRA_PATTERN = Pattern.compile("[A-Z]+\\-\\d+");
  private boolean myShared;

  public JiraRepository(TaskRepositoryType type) {
    this();
    myType = type;
  }

  /** for serialization */
  public JiraRepository() {
    super("", new ServerId());
  }

  private JiraRepository(JiraRepository other) {
    super(other);
    myType = other.getType();
    myShared = other.isShared();
  }

  public boolean isConfigured() {
    return StringUtil.isNotEmpty(getUrl()) && StringUtil.isNotEmpty(getUsername());
  }

  public boolean isShared() {
    return myShared;
  }

  public Task[] getMyIssues(int count)  {
    try {
      List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
      String username = getUsername();
      query.add(new JIRAAssigneeBean(0, "Assignee", username));
      query.add(new JIRAResolutionBean(-1, JiraConstants.UNRESOLVED));
      List<JIRAIssue> list = JIRAServerFacadeImpl.getInstance().getIssues(this, query, "updated", "DESC", 0, 30);
      return ContainerUtil.map2Array(list, Task.class, new Function<JIRAIssue, Task>() {
        public Task fun(final JIRAIssue jiraIssue) {
          return new JiraTask(jiraIssue);
        }
      });
    }
    catch (JIRAException e) {
      LOG.info(e);
      return new Task[0];
    }
  }

  public String getPresentableName() {
    return getUrl() == null ? "<undefined>" : getUrl();
  }

  public TaskRepositoryType getType() {
    return myType;
  }

  public void testConnection() throws RemoteApiException {
    JIRAServerFacadeImpl.getInstance().testServerConnection(this);
  }

  public TaskRepository clone() {
    return new JiraRepository(this);
  }

  @Nullable
  public Task findTask(String id) {
    try {
      JIRAIssue issue = JIRAServerFacadeImpl.getInstance().getIssue(this, id);
      return new JiraTask(issue);
    }
    catch (JIRAException e) {
      LOG.info(e);
      return null;
    }
  }

  public void closeTask(Task task) {
//    JIRAServerFacadeImpl.getInstance().
  }

  @Nullable
  public String extractId(String taskName) {
    Matcher matcher = JIRA_PATTERN.matcher(taskName);
    return matcher.find() ? matcher.group() : null;
  }

  public String getTaskUrl(Task task) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void setShared(boolean shared) {
    myShared = shared;
  }

  @Override
  public int hashCode() {
    return getUrl().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof JiraRepository && super.equals(o) && myShared == ((JiraRepository)o).isShared();
  }
}
