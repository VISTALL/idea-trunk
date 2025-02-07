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

package com.intellij.tasks;

import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.util.xmlb.annotations.Attribute;

/**
 * @author Dmitry Avdeev
 */
public class ChangeListInfo {

  @Attribute("id")
  public String id;

  @Attribute("name")
  public String name;
  
  @Attribute("comment")
  public String comment;

  /** For serialization */
  @SuppressWarnings({"UnusedDeclaration"})
  public ChangeListInfo() {
  }

  public ChangeListInfo(LocalChangeList changeList) {
    id = changeList.getId();
    name = changeList.getName();
    comment = changeList.getComment();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ChangeListInfo that = (ChangeListInfo)o;

    return !(id != null ? !id.equals(that.id) : that.id != null);

  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
