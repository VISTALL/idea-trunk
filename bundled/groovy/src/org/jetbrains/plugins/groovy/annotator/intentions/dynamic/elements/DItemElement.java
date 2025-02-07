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
package org.jetbrains.plugins.groovy.annotator.intentions.dynamic.elements;

import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.elements.DNamedElement;
import org.jetbrains.plugins.groovy.annotator.intentions.dynamic.elements.DTypedElement;
import org.jetbrains.annotations.NotNull;

/**
 * User: Dmitry.Krasilschikov
 * Date: 13.02.2008
 */

/*
 * Base class for Dynamic property and method
 */

public abstract class DItemElement implements DNamedElement, DTypedElement {
  public String myType = null;
  public Boolean myStatic = false;
  public String myName = null;

//  @NotNull
  public String myHightlightedText = null;

  public DItemElement(Boolean isStatic, String name, String type) {
    myStatic = isStatic;
    myName = name;
    myType = type;
  }

  public String getHightlightedText() {
    return myHightlightedText;
  }

  public void setHightlightedText(String hightlightedText) {
    myHightlightedText = hightlightedText;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DItemElement that = (DItemElement) o;

    if (myName != null ? !myName.equals(that.myName) : that.myName != null) return false;
    if (myType != null ? !myType.equals(that.myType) : that.myType != null) return false;
    if (myStatic != that.myStatic) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (myType != null ? myType.hashCode() : 0);
    result = 31 * result + (myName != null ? myName.hashCode() : 0);
    return result;
  }

  public String getType() {
    return myType;
  }

  public void setType(String type) {
    this.myType = type;
    clearCache();
  }

  public abstract void clearCache();

  public String getName() {
    return myName;
  }

  public void setName(String name) {
    this.myName = name;
    clearCache();
  }

  public Boolean isStatic() {
      return myStatic;
  }

  public void setStatic(Boolean aStatic) {
    myStatic = aStatic;
    clearCache();
  }

  @NotNull
  public abstract PsiNamedElement getPsi(PsiManager manager, String containingClassName);
}