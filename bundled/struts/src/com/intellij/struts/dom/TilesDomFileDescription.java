/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package com.intellij.struts.dom;

import com.intellij.psi.xml.XmlFile;
import com.intellij.struts.StrutsProjectComponent;
import com.intellij.struts.StrutsConstants;
import com.intellij.struts.dom.tiles.TilesDefinitions;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * DOM FileDescription for tiles plugin.
 *
 * @author Dmitry Avdeev
 */
public class TilesDomFileDescription extends StrutsPluginDescriptorBase<TilesDefinitions> {

  public TilesDomFileDescription() {
    super(TilesDefinitions.class, TilesDefinitions.TILES_DEFINITIONS);
  }

  @NotNull
  protected Set<XmlFile> getFilesToMerge(final DomElement element) {
    return StrutsProjectComponent.getInstance(element.getManager().getProject()).getTilesFactory().getConfigFiles(element.getXmlElement());
  }

  protected void initializeFileDescription() {
    registerNamespacePolicy(StrutsConstants.TILES_DOM_NAMESPACE_KEY, StrutsConstants.TILES_DTDS);
  }

}