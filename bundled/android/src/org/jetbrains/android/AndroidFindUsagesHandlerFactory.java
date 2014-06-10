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

package org.jetbrains.android;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.android.dom.resources.ResourceElement;
import org.jetbrains.android.dom.wrappers.ResourceElementWrapper;
import org.jetbrains.android.dom.wrappers.ValueResourceElementWrapper;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 5, 2009
 * Time: 4:00:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidFindUsagesHandlerFactory extends FindUsagesHandlerFactory {
  @Override
  public boolean canFindUsages(PsiElement element) {
    if (element instanceof XmlAttributeValue) {
      XmlAttributeValue value = (XmlAttributeValue)element;
      if (AndroidResourceUtil.findIdField(value) != null) {
        return true;
      }
    }
    element = correctResourceElement(element);
    if (element instanceof PsiField) {
      return AndroidResourceUtil.isResourceField((PsiField)element);
    }
    else if (element instanceof PsiFile) {
      return AndroidResourceUtil.findResourceFieldForFileResource((PsiFile)element) != null;
    }
    else if (element instanceof XmlTag) {
      return AndroidResourceUtil.findResourceFieldForValueResource((XmlTag)element) != null;
    }
    return false;
  }

  private static class MyFindUsagesHandler extends FindUsagesHandler {
    private final PsiElement[] myAdditionalElements;

    protected MyFindUsagesHandler(@NotNull PsiElement element, PsiElement... additionalElements) {
      super(element);
      myAdditionalElements = additionalElements;
    }

    @NotNull
    @Override
    public PsiElement[] getSecondaryElements() {
      return myAdditionalElements;
    }
  }

  @Nullable
  private static PsiElement correctResourceElement(PsiElement element) {
    if (element instanceof XmlElement) {
      XmlTag tag = element instanceof XmlTag ? (XmlTag)element : PsiTreeUtil.getParentOfType(element, XmlTag.class);
      DomElement domElement = DomManager.getDomManager(element.getProject()).getDomElement(tag);
      if (domElement instanceof ResourceElement) {
        return tag;
      }
      return null;
    }
    return element;
  }

  private static XmlAttributeValue wrapIfNeccessary(XmlAttributeValue value) {
    if (value instanceof ResourceElementWrapper) {
      return value;
    }
    return new ValueResourceElementWrapper(value);
  }

  @Override
  public FindUsagesHandler createFindUsagesHandler(PsiElement element, boolean forHighlightUsages) {
    AndroidFacet facet = AndroidFacet.getInstance(element);
    assert facet != null;
    if (element instanceof XmlAttributeValue) {
      XmlAttributeValue value = (XmlAttributeValue)element;
      PsiField field = AndroidResourceUtil.findIdField(value);
      if (field != null) {
        element = wrapIfNeccessary(value);
        return new MyFindUsagesHandler(element, field);
      }
    }
    element = correctResourceElement(element);
    assert element != null;
    if (element instanceof PsiFile) {
      // resource file
      PsiField field = AndroidResourceUtil.findResourceFieldForFileResource((PsiFile)element);
      assert field != null;
      return new MyFindUsagesHandler(element, field);
    }
    else if (element instanceof XmlTag) {
      // value resource
      XmlTag tag = (XmlTag)element;
      PsiField field = AndroidResourceUtil.findResourceFieldForValueResource(tag);
      assert field != null;
      XmlAttributeValue nameValue = AndroidUtils.getNameAttrValue(tag);
      assert nameValue != null;
      return new MyFindUsagesHandler(nameValue, field);
    }
    else if (element instanceof PsiField) {
      List<PsiElement> resources = AndroidResourceUtil.findResourcesByField((PsiField)element);
      if (resources.size() == 0) {
        return new MyFindUsagesHandler(element);
      }
      // ignore alternative resources because their usages are the same
      PsiElement resource = resources.get(0);
      if (resource instanceof XmlAttributeValue) {
        resource = wrapIfNeccessary((XmlAttributeValue)resource);
      }
      return new MyFindUsagesHandler(element, resource);
    }
    return null;
  }
}
