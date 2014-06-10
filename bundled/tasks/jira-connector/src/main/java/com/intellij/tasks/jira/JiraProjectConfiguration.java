/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.tasks.jira;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ArrayList;

/**
 * @author Dmitry Avdeev
 */
@State(
    name = "JiraServers",
    storages = {@Storage(id = "default", file = "$WORKSPACE_FILE$")})
public class JiraProjectConfiguration implements PersistentStateComponent<JiraProjectConfiguration> {
  public static JiraProjectConfiguration getConfiguration(Project project) {
    return ServiceManager.getService(project, JiraProjectConfiguration.class);
  }

  private final Shared myShared;
  private LinkedHashSet<JiraRepository> myServers = new LinkedHashSet<JiraRepository>();

  public JiraProjectConfiguration() {
    myShared = null;
  }

  public JiraProjectConfiguration(Shared shared) {
    myShared = shared;
  }

  public JiraProjectConfiguration getState() {
    return this;
  }

  public void loadState(JiraProjectConfiguration state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @AbstractCollection(surroundWithTag = false)
  public Collection<JiraRepository> getServers() {
    if (myShared != null) {
      for (final JiraRepository shared : myShared.getServers()) {
        JiraRepository repository = ContainerUtil.find(myServers, new Condition<JiraRepository>() {
          public boolean value(JiraRepository jiraRepository) {
            return Comparing.equal(jiraRepository.getUrl(), shared.getUrl());
          }
        });
        if (repository == null) {
          myServers.add(shared);
        }
      }
    }
    return new ArrayList<JiraRepository>(myServers);
  }

  public void setServers(Collection<JiraRepository> servers) {
    myServers.clear();
    myServers.addAll(servers);
    if (myShared != null) {
      myShared.setServers(ContainerUtil.mapNotNull(servers, new NullableFunction<JiraRepository, JiraRepository>() {
        public JiraRepository fun(JiraRepository jiraRepository) {
          if (!jiraRepository.isShared()) {
            return null;
          }
          JiraRepository repository = new JiraRepository();
          repository.setUrl(jiraRepository.getUrl());
          return repository;
        }
      }));
    }
  }

  @State(
    name = "SharedJiraServers",
    storages = {@Storage(id = "default", file = "$PROJECT_FILE$")})
  public static class Shared extends JiraProjectConfiguration {
  }
}
