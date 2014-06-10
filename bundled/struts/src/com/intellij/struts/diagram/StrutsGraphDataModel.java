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

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Aug 21, 2006
 * Time: 12:14:52 PM
 */
package com.intellij.struts.diagram;

import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.graph.builder.CachedGraphDataModel;
import com.intellij.struts.StrutsIcons;
import com.intellij.struts.dom.Action;
import com.intellij.struts.dom.Exception;
import com.intellij.struts.dom.Forward;
import com.intellij.struts.dom.StrutsConfig;
import com.intellij.util.xml.GenericValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class StrutsGraphDataModel extends CachedGraphDataModel<StrutsObject, StrutsObject> {

  private final StrutsConfig myStrutsConfig;

  public StrutsGraphDataModel(final StrutsConfig strutsConfig) {
    myStrutsConfig = strutsConfig;
  }


  /**
   * Creates node and corresponding edge.
   *
   * @param source      source node
   * @param name        Edge name.
   * @param pathValue   WebPath.
   * @param reverse     Reverts edge direction.
   */
  protected void createEdge(final StrutsObject source, final @NonNls String name, final GenericValue<PathReference> pathValue, boolean reverse) {

    final PathReference webPath = pathValue.getValue();
    @NotNull final StrutsObject target;
    if (webPath != null) {
      target = new StrutsNodeObject(webPath);
    } else {
      final String path = pathValue.getStringValue();
      if (path == null || path.length() == 0) {
        return;
      }
      target = new StrutsNodeObject(path);
    }

    createNode(target);

    final StrutsObject edge = new StrutsEdgeObject(source.getName(), name);
    if (reverse) {
      createEdge(edge, target, source);
    } else {
      createEdge(edge, source, target);
    }
  }

  public StrutsObject createEdge(@NotNull final StrutsObject from, @NotNull final StrutsObject to) {
    return super.createEdge(from, to);
  }

  protected void buildGraph() {
    clear();

    for (final Forward forward : myStrutsConfig.getGlobalForwards().getForwards()) {
      final String name = forward.getName().getStringValue(); //forward.getGenericInfo().getElementName(forward);
      if (name != null) {
        final StrutsObject source = new StrutsNodeObject(name, StrutsIcons.GLOBAL_FORWARD_ICON, forward.getXmlTag());
        createNode(source);
        createEdge(source, "", forward.getPath(), false);
      }
    }

    for (final Exception exception : myStrutsConfig.getGlobalExceptions().getExceptions()) {
      final String name = exception.getKey().getStringValue(); //exception.getGenericInfo().getElementName(exception);
      if (name != null) {
        final StrutsObject source = new StrutsNodeObject(name, StrutsIcons.EXCEPTION_ICON, exception.getXmlTag());
        createNode(source);
        createEdge(source, "", exception.getPath(), false);
      }
    }

    for (final Action action : myStrutsConfig.getActionMappings().getActions()) {
      final String name = action.getPath().getStringValue(); //action.getGenericInfo().getElementName(action);
      if (name != null) {
        final StrutsObject source = new StrutsNodeObject(name, StrutsIcons.ACTION_ICON, action.getXmlTag());
        createNode(source);

        for (final Forward forward : action.getForwards()) {
          createEdge(source, forward.getName().getStringValue(), forward.getPath(), false);
        }

        for (final Exception exception : action.getExceptions()) {
          createEdge(source, exception.getKey().getStringValue(), exception.getPath(), false);
        }

        createEdge(source, "forward", action.getForward(), false);
        createEdge(source, "include", action.getInclude(), false);
        createEdge(source, "input", action.getInput(), true);
      }
    }
  }

  @NotNull
  public String getNodeName(final StrutsObject strutsObject) {
    return strutsObject.getName();
  }

  @NotNull
  public String getEdgeName(final StrutsObject strutsObject) {
    return strutsObject.getName();
  }
}