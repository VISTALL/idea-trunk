/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.idea.perforce.perforce.ConnectionId;

import java.io.File;

public class SingletonConnection extends AbstractP4Connection {

  private final static Key<SingletonConnection> KEY_IN_PROJECT = new Key<SingletonConnection>("Connection per project");
  public static final ConnectionId SINGLETON_CONNECTION_ID = new ConnectionId();

  private SingletonConnection() {
  }

  public static SingletonConnection getInstance(Project project){
    SingletonConnection result = project.getUserData(KEY_IN_PROJECT);
    if (result == null) {
      result = new SingletonConnection();
      project.putUserData(KEY_IN_PROJECT, result);
    }
    return result;
  }

  public ConnectionId getId() {
    return SINGLETON_CONNECTION_ID;
  }

  public boolean handlesFile(File file) {
    return true;
  }

  protected File getCwd() {
    return new File(".");
  }
}
