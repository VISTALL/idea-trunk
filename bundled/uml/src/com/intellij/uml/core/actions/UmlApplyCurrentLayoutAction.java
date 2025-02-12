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

import com.intellij.uml.UmlEdge;
import com.intellij.uml.UmlNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.actions.AbstractGraphAction;
import com.intellij.openapi.graph.builder.actions.layout.ApplyCurrentLayoutAction;

/**
 * @author Konstantin Bulenkov
 */
public class UmlApplyCurrentLayoutAction extends StandardGraphActionsWrapper {
  public AbstractGraphAction getGraphAction(AnActionEvent e) {
    final GraphBuilder<UmlNode, UmlEdge> builder = getBuilder(e);
    return (builder == null) ? null : new ApplyCurrentLayoutAction(builder.getGraph());
  }
}
