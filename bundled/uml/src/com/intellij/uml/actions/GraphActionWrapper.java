/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.actions;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Konstantin Bulenkov
 */
public abstract class GraphActionWrapper extends AbstractAction {
  private final AbstractAction myAction;
  private final GraphBuilder<UmlNode, UmlEdge> myBuilder;

  public GraphActionWrapper(@NotNull AbstractAction action, GraphBuilder<UmlNode, UmlEdge> builder) {
    myAction = action;
    myBuilder = builder;
  }

  protected AbstractAction getAction() {
    return myAction;
  }

  public GraphBuilder<UmlNode, UmlEdge> getBuilder() {
    return myBuilder;
  }

  public abstract void actionPerformed(final ActionEvent e);

  @Override
  public Object getValue(final String key) {
    return myAction.getValue(key);
  }

  @Override
  public void putValue(final String key, final Object newValue) {
    myAction.putValue(key, newValue);
  }

  @Override
  public boolean isEnabled() {
    return myAction.isEnabled();
  }

  @Override
  public void setEnabled(final boolean newValue) {
    myAction.setEnabled(newValue);
  }

  @Override
  public Object[] getKeys() {
    return myAction.getKeys();
  }

  @Override
  public synchronized void addPropertyChangeListener(final PropertyChangeListener listener) {
    myAction.addPropertyChangeListener(listener);
  }

  @Override
  public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
    myAction.removePropertyChangeListener(listener);
  }

  @Override
  public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
    return myAction.getPropertyChangeListeners();
  }

  @Override
  public String toString() {
    return myAction.toString();
  }
}

