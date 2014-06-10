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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.Graph2DViewActions;
import com.intellij.uml.*;
import com.intellij.uml.actions.MoveSelectionWrapper;
import com.intellij.uml.core.actions.visibility.ChangeVisibilityGroup;
import com.intellij.uml.core.actions.visibility.ChangeVisibilityLevelAction;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Konstantin Bulenkov
 */
public class UmlActions {
  @NonNls private static final String SHOW_PACKAGES_ACTION = "SHOW_PACKAGES";
  @NonNls private static final String HIDE_PACKAGES_ACTION = "HIDE_PACKAGES";
  @NonNls private static final String FOCUS_TOP_NODE = "FOCUS_TOP_NODE";
  @NonNls private static final String FOCUS_BOTTOM_NODE = "FOCUS_BOTTOM_NODE";
  @NonNls private static final String FOCUS_LEFT_NODE = "FOCUS_LEFT_NODE";
  @NonNls private static final String FOCUS_RIGHT_NODE = "FOCUS_RIGHT_NODE";
  @NonNls private static final String DELETE_SELECTION = "DELETE_SELECTION";
  @NonNls private static final String EDIT_NODE = "EDIT_NODE";

  private UmlActions() {
  }

  public static void install(GraphBuilder myBuilder) {
    Graph2DView view = myBuilder.getView();
    Graph2DViewActions actions = GraphManager.getGraphManager().createGraph2DViewActions(view);
    ActionMap actionMap = actions.createActionMap();
    InputMap inputMap = actions.createDefaultInputMap(actionMap);
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false), EDIT_NODE);
    //inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), SHOW_PACKAGES_ACTION);
    //actionMap.put(SHOW_PACKAGES_ACTION, new SelectClassesFromPackage(myBuilder, true));
    //inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), HIDE_PACKAGES_ACTION);
    //actionMap.put(HIDE_PACKAGES_ACTION, new SelectClassesFromPackage(myBuilder, false));
    setMoveActions(actionMap, myBuilder, FOCUS_TOP_NODE, FOCUS_BOTTOM_NODE, FOCUS_LEFT_NODE, FOCUS_RIGHT_NODE);
    wrapDeleteAction(actionMap, myBuilder);

    view.getCanvasComponent().setActionMap(actionMap);
    view.getCanvasComponent().setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
  }

  private static final Map<String, MoveSelectionWrapper.Keys> MOVE_KEYS = new HashMap<String, MoveSelectionWrapper.Keys>();
  static {
    MOVE_KEYS.put(FOCUS_TOP_NODE, MoveSelectionWrapper.Keys.UP);
    MOVE_KEYS.put(FOCUS_BOTTOM_NODE, MoveSelectionWrapper.Keys.DOWN);
    MOVE_KEYS.put(FOCUS_LEFT_NODE, MoveSelectionWrapper.Keys.LEFT);
    MOVE_KEYS.put(FOCUS_RIGHT_NODE, MoveSelectionWrapper.Keys.RIGHT);
  }

  private static void setMoveActions(ActionMap aMap, GraphBuilder builder, String... actions) {
    for (String actionName : actions) {
      Object action = aMap.get(actionName);
      if (action instanceof AbstractAction) {
        aMap.put(actionName, new MoveSelectionWrapper(MOVE_KEYS.get(actionName), builder.getGraph()));
      }
    }
  }

  private static void wrapDeleteAction(ActionMap aMap, GraphBuilder builder) {
    AbstractAction action = (AbstractAction)aMap.get(DELETE_SELECTION);
    DeleteSelectionWrapper wrapper = new DeleteSelectionWrapper(action, builder);
    aMap.put(DELETE_SELECTION, wrapper);
  }

  public static DefaultActionGroup createToolbarActions(GraphBuilder<UmlNode, UmlEdge> builder) {
    final ActionManager am = ActionManager.getInstance();
    DefaultActionGroup group = new DefaultActionGroup();
    UmlProvider provider = Utils.getProvider(builder);
    final UmlCategory[] categories = provider.getNodeContentManager().getContentCategories();
    for (UmlCategory category : categories) {
      group.add(new UmlCategorySwitcher(category, builder));
    }
    List<AnAction> visibilityActions = new ArrayList<AnAction>();
    for (VisibilityLevel level : provider.getVisibilityManager().getVisibilityLevels()) {
      visibilityActions.add(new ChangeVisibilityLevelAction(provider.getVisibilityManager(), level, builder));
    }
    if (visibilityActions.size() > 0) {
      group.add(new ChangeVisibilityGroup(visibilityActions.toArray(new AnAction[visibilityActions.size()])));
    }
    group.addSeparator();
    group.add(am.getAction("Uml.Standard.Toolbar.Actions"));
    return group;
  }

  public static void registerCustomShortcuts(final GraphBuilder graphBuilder) {
    JComponent comp = graphBuilder.getView().getCanvasComponent();
    final ActionManager am = ActionManager.getInstance();
    @NonNls Map<String, KeyStroke>  map = new HashMap<String, KeyStroke>();
    map.put("Uml.CollapseNodes", KeyStroke.getKeyStroke('c'));
    map.put("Uml.ExpandNodes", KeyStroke.getKeyStroke('e'));
    for (String action : map.keySet()) {
      am.getAction(action).registerCustomShortcutSet(new CustomShortcutSet(map.get(action)), comp);
    }
  }
}
