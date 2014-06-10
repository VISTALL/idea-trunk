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

package com.intellij.struts;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.struts.diagram.StrutsGraphDataModel;
import com.intellij.struts.diagram.StrutsObject;
import com.intellij.struts.dom.Action;
import com.intellij.struts.dom.ActionMappings;
import com.intellij.struts.dom.FormBean;
import com.intellij.struts.dom.StrutsConfig;
import com.intellij.struts.dom.tiles.Definition;
import com.intellij.struts.dom.tiles.TilesDefinitions;

import java.util.Collection;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class StrutsModelTest extends StrutsTest {

  public void testStrutsConfig() {
    final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(getTestDataPath() + "/WEB-INF/struts-config.xml");
    List<StrutsModel> model = StrutsManager.getInstance().getAllStrutsModels(getModule());
    assertEquals("Config not found", 1, model.size());
    StrutsConfig config = model.get(0).getMergedModel();

    ActionMappings mappings = config.getActionMappings();
    assertEquals(10, mappings.getActions().size());
  }

  public void testTilesDefinitions() {

    List<TilesModel> model = StrutsManager.getInstance().getAllTilesModels(getModule());
    assertNotNull("Tiles config not found", model.size() != 1);
    TilesDefinitions config = model.get(0).getMergedModel();
    List<Definition> defs = config.getDefinitions();
    assertEquals(4, defs.size());
  }

  public void testStrutsModel() {

    StrutsModel model = StrutsManager.getInstance().getAllStrutsModels(getModule()).get(0);
    assertNotNull("Config not found", model);

    final int configsCount = 2;
    assertEquals(configsCount, model.getConfigFiles().size());

    final int actionsCount = 10;
    List<Action> actions = model.getActions();
    assertEquals(actionsCount, actions.size());

    String[] paths = model.getActionPaths();
    assertEquals(8, paths.length);

    Action login = model.findAction("/login");
    assertNotNull("Login action not found", login);

    Action another = model.findAction("/anotherAction");
    assertNotNull("Another action not found", another);

    FormBean form = model.findFormBean("loginForm");
    assertNotNull("Login form not found", form);
  }

  public void testDiagramModel() {
    List<StrutsModel> model = StrutsManager.getInstance().getAllStrutsModels(getModule());
    assertNotNull("Config not found", model.size() != 1);
    StrutsConfig config = model.get(0).getMergedModel();
    final StrutsGraphDataModel graphDataModel = new StrutsGraphDataModel(config);
    final Collection<StrutsObject> nodes = graphDataModel.getNodes();
    assertEquals(26, nodes.size());    
    final Collection<StrutsObject> edges = graphDataModel.getEdges();
    assertEquals(12, edges.size());
  }
}
