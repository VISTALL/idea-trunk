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

package com.intellij.uml.core.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.uml.*;
import com.intellij.uml.utils.NewUmlUtils;

/**
 * @author Konstantin Bulenkov
 */
public class UmlCategorySwitcher extends AnAction implements Toggleable {
  private final UmlCategory myCategory;
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private final UmlProvider myProvider;

  public UmlCategorySwitcher(UmlCategory category, GraphBuilder<UmlNode, UmlEdge> builder) {
    super("Show " + category.getName(), "", category.getIcon());
    myCategory = category;
    myBuilder = builder;
    myProvider = Utils.getProvider(builder);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    NewUmlUtils.getPresentation(myBuilder).setCategoryEnabled(myCategory, !isEnabled());
    Utils.updateGraph(myBuilder);
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().putClientProperty(SELECTED_PROPERTY, isEnabled());
  }

  private boolean isEnabled() {
    return NewUmlUtils.getPresentation(myBuilder).isCategoryEnabled(myCategory);
  }
}
