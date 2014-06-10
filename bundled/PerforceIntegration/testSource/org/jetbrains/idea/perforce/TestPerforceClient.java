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
package org.jetbrains.idea.perforce;

import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.perforce.View;

import java.util.List;

public class TestPerforceClient implements PerforceClient {
  private final String myUserName;
  private final String myName;
  private final String myRoot;
  private final List<View> myViews;
  private final String myServerPort = "server_port";

  public TestPerforceClient(final String name, final String userName, final String root, final List<View> views) {
    myUserName = userName;
    myName = name;
    myRoot = root;
    myViews = views;
  }

  public String getName() {
    return myName;
  }

  public String getRoot() {
    return myRoot;
  }

  public List<View> getViews() {
    return myViews;
  }

  public String getUserName() {
    return myUserName;
  }

  public String getServerPort() {
    return myServerPort;
  }
}
