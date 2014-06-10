package com.intellij.tasks.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.util.HttpConfigurableIdeaImpl;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.util.Consumer;
import com.intellij.util.xmlb.XmlSerializerUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class JiraRepositoryType implements TaskRepositoryType {

  private final List<JiraRepository> myRepositories = new ArrayList<JiraRepository>();
  private JiraProjectConfiguration myProjectConfiguration;

  public JiraRepositoryType(Project project) {
    PluginConfigurationBean configurationBean = new PluginConfigurationBean();
    configurationBean.transientSetHttpConfigurable(HttpConfigurableIdeaImpl.getInstance());
    ConfigurationFactory.setConfiguration(configurationBean);

    myProjectConfiguration = JiraProjectConfiguration.getConfiguration(project);
    for (JiraServerCfg server : myProjectConfiguration.getServers()) {
      JiraRepository repository = createRepository();
      XmlSerializerUtil.copyBean(server, repository);
      myRepositories.add(repository);
    }
  }

  public String getName() {
    return "JIRA";
  }

  public Icon getIcon() {
    return IconLoader.getIcon("/resources/jira-blue-16.png");
  }

  public UnnamedConfigurable createEditor(final TaskRepository repository, Project project, final Consumer<TaskRepository> changeListener) {
    return new BaseRepositoryEditor(project, repository) {
      public void apply() {
        ((JiraRepository)repository).setUrl(myURLText.getText());
        ((JiraRepository)repository).setUsername(myUserNameText.getText());
        ((JiraRepository)repository).setPassword(myPasswordText.getText());
        ((JiraRepository)repository).setShared(myShareURL.isSelected());
        changeListener.consume(repository);
      }

      public void doReset() {
        myURLText.setText(((JiraRepository)repository).getUrl());
        myUserNameText.setText(((JiraRepository)repository).getUsername());
        myPasswordText.setText(((JiraRepository)repository).getPassword());
        myShareURL.setSelected(repository.isShared());
      }
    };
  }

  public TaskRepository[] getRepositories() {
    return myRepositories.toArray(new TaskRepository[myRepositories.size()]);    
  }

  public JiraRepository createRepository() {
    return new JiraRepository(this);
  }

  public void configureRepositories(TaskRepository[] newRepositories) {
    myRepositories.clear();
    for (TaskRepository repository : newRepositories) {
      myRepositories.add((JiraRepository)repository);
    }
    myProjectConfiguration.setServers(myRepositories);
  }
}
