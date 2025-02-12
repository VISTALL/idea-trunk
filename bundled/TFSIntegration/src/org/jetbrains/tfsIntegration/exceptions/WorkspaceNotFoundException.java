/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.tfsIntegration.exceptions;

import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;

public class WorkspaceNotFoundException extends TfsException {

  private static final long serialVersionUID = 1L;

  public static final String CODE = "WorkspaceNotFoundException";

  private final String myWorkspaceOwner;

  private final String myWorkspaceName;

  public WorkspaceNotFoundException(final AxisFault cause) {
    super(cause);

    if (cause.getDetail() != null) {
      myWorkspaceOwner = cause.getDetail().getAttributeValue(new QName("WorkspaceOwner"));
      myWorkspaceName = cause.getDetail().getAttributeValue(new QName("WorkspaceName"));
    }
    else {
      myWorkspaceOwner = null;
      myWorkspaceName = null;
    }

  }

  public String getWorkspaceName() {
    return myWorkspaceName;
  }

  public String getWorkspaceOwner() {
    return myWorkspaceOwner;
  }
}
