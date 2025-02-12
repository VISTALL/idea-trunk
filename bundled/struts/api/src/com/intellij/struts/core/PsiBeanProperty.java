/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package com.intellij.struts.core;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: DAvdeev
 * Date: 26.01.2006
 * Time: 18:38:52
 * To change this template use File | Settings | File Templates.
 */
public interface PsiBeanProperty {

  PsiBeanProperty[] EMPTY_ARRAY = new PsiBeanProperty[0];

  PsiElement[] getPsiElements();

  Icon getIcon();

  String getName();

  String getType();

  boolean hasSetter();

  boolean hasGetter();

  PsiMethod getGetter();
}
