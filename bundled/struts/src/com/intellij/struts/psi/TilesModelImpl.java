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

package com.intellij.struts.psi;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts.NamedModelImpl;
import com.intellij.struts.TilesModel;
import com.intellij.struts.dom.tiles.Definition;
import com.intellij.struts.dom.tiles.Put;
import com.intellij.struts.dom.tiles.TilesDefinitions;
import com.intellij.struts.util.DomNamedElementsHashingStrategy;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomUtil;
import gnu.trove.THashSet;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


/**
 * Provides functionality for accessing DOM-Model of <code>tiles-defs.xml</code> files.
 */
public class TilesModelImpl extends NamedModelImpl<TilesDefinitions> implements TilesModel {

  public TilesModelImpl(@NotNull Set<XmlFile> configFiles, @NotNull DomFileElement<TilesDefinitions> mergedModel, String name) {
    super(configFiles, mergedModel, name);
  }

  @Nullable
  public XmlTag getTileTag(final String definitionName) {
    final Definition def = findDefinition(definitionName);
    return def == null ? null : def.getName().getXmlTag();
  }

  @NotNull
  public List<Definition> getDefinitions() {
    return getMergedModel().getDefinitions();
  }

  @Nullable
  public Definition findDefinition(final String definitionName) {
    final List<Definition> defs = getDefinitions();
    return DomUtil.findByName(defs, definitionName);
  }

  @Nullable
  public Set<Put> getPuts(final String definitionName, boolean includingExtends) {
    Set<Put> set = new THashSet<Put>(putHashingStrategy);
    boolean found = getPuts(findDefinition(definitionName), set, new HashSet<Definition>(), includingExtends);
    if (!found) {
      return null;
    }
    return set;
  }

  @Nullable
  public Collection<Put> getAllPuts(final String definitionName) {
    ArrayList<Put> set = new ArrayList<Put>();
    boolean found = getPuts(findDefinition(definitionName), set, new HashSet<Definition>(), true);
    if (!found) {
      return null;
    }
    return set;
  }

  @Nullable
  public XmlTag getPutTag(final String definitionName, final String putName) {
    Definition def = findDefinition(definitionName);
    if (def == null) {
      return null;
    }
    Put put = findPutDefinition(def, putName, new HashSet<Definition>());
    return put == null ? null : put.getName().getXmlTag();
  }

  @Nullable
  private static Put findPutDefinition(final Definition def, final String putName, final Set<Definition> visited) {
    Definition extend = def.getExtends().getValue();
    if (extend != null && !visited.contains(def)) {
      visited.add(extend);
      final Put put = findPutDefinition(extend, putName, visited);
      if (put != null) {
        return put;
      }
    }
    return DomUtil.findByName(def.getPuts(), putName);
  }

  private final static TObjectHashingStrategy<Put> putHashingStrategy = new DomNamedElementsHashingStrategy<Put>();

  private static boolean getPuts(final Definition definition, final Collection<Put> puts, final Set<Definition> visited, boolean includingExtends) {

    if (definition == null || visited.contains(definition)) {
      return false;
    }
    visited.add(definition);

    final List<Put> result = definition.getPuts();
    if (result != null) {
      for (Put put : result) {
        if (put.getName().getValue() != null) {
          puts.add(put);
        }
      }
    }
    if (includingExtends) {
      final Definition extend = definition.getExtends().getValue();
      if (extend != null) {
        getPuts(extend, puts, visited, true);
      }
    }
    return true;
  }
}
