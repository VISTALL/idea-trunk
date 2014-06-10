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
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.View;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PerforceClientImpl implements PerforceClient {
  private final Project myProject;
  private final P4Connection myConnection;
  private Map<String, List<String>> myCachedInfo;
  private List<View> myViews;

  public PerforceClientImpl(final Project project, P4Connection connection) {
    myProject = project;
    myConnection = connection;
  }

  private Map<String, List<String>> getInfo(){
    if (myCachedInfo == null || myCachedInfo.size() == 0) {
      myCachedInfo = PerforceManager.getInstanceChecked(myProject).getCachedInfo(myConnection);
    }

    return myCachedInfo;
  }

  public String getName() {
    return getFieldValue(PerforceRunner.CLIENT_NAME);
  }

  public String getRoot() {
    return PerforceManager.getInstanceChecked(myProject).getClientRoot(myConnection);
  }

  public List<View> getViews() {
    if (myViews == null) {
      Map<String, List<String>> clientSpec = PerforceManager.getInstance(myProject).getCachedClients(myConnection);
      myViews = parseViews(clientSpec);
    }

    return myViews;
  }

  private static List<View> parseViews(Map<String, List<String>> clientSpec) {
    final List<String> list = clientSpec.get(PerforceRunner.VIEW);

    final ArrayList<View> result = new ArrayList<View>();
    if (list != null) {
      for (final String aList : list) {
        final View view = View.create(aList);
        if (view != null) {
          result.add(view);
        }
      }
    }
    return result;
  }

  private String getFieldValue(final String fieldName) {
    final List<String> names = getInfo().get(fieldName);
    if (names == null || names.isEmpty()) {
      return null;
    } else {
      return names.get(0);
    }
  }

  public String getUserName() {
    return getFieldValue(PerforceRunner.USER_NAME);
  }

  public String getServerPort() {
    return getFieldValue(PerforceRunner.SERVER_ADDRESS);
  }
}
