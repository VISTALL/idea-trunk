/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.perspectives.graph;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Dmitry.Krasilschikov
 * Date: 06.08.2007
 */
public class DomainClassNode {
  private final PsiClass myTypeDefinition;
  @Nullable
  public static final Collection<DomainClassNode> EMPPTY_LIST = new ArrayList<DomainClassNode>();

  public DomainClassNode(@NotNull PsiClass typeDefinition) {
    myTypeDefinition = typeDefinition;
  }

  @NotNull
  public String getUniqueName() {
    final String qualifiedName = myTypeDefinition.getQualifiedName();
    if (qualifiedName != null) {
      return qualifiedName;
    }

    return myTypeDefinition.getName();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DomainClassNode that = (DomainClassNode) o;

    return !(myTypeDefinition != null ? !myTypeDefinition.equals(that.myTypeDefinition) : that.myTypeDefinition != null);

  }

  public int hashCode() {
    return (myTypeDefinition != null ? myTypeDefinition.hashCode() : 0);
  }

  @NotNull
  public PsiClass getTypeDefinition() {
    return myTypeDefinition;
  }
}
