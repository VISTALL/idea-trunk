/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.tiles;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts.StrutsIcons;
import com.intellij.struts.StrutsManager;
import com.intellij.struts.TilesModel;
import com.intellij.struts.dom.tiles.Definition;
import com.intellij.struts2.dom.struts.impl.path.StrutsResultContributor;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Provides path to Tiles definition.
 *
 * @author Yann C&eacute;bron
 */
public class TilesResultContributor extends StrutsResultContributor {

  @NonNls
  private static final String TILES_RESULT_TYPE = "tiles";

  @Override
  protected boolean matchesResultType(@NonNls @Nullable final String resultType) {
    return Comparing.equal(resultType, TILES_RESULT_TYPE);
  }

  public boolean createReferences(@NotNull final PsiElement psiElement,
                                  @NotNull final List<PsiReference> psiReferences,
                                  final boolean soft) {
    final String currentPackage = getNamespace(psiElement);
    if (currentPackage == null) {
      return false;
    }

    final List<TilesModel> allTilesModels = getAllTilesModels(psiElement);
    psiReferences.add(new TilesDefinitionReference((XmlTag) psiElement, allTilesModels));
    return false;
  }

  public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement psiElement) {
    final String currentPackage = getNamespace(psiElement);
    if (currentPackage == null) {
      return null;
    }

    return new PathReference(path, new PathReference.ConstFunction(StrutsIcons.TILE_ICON)) {
      @Override
      public PsiElement resolve() {
        for (final TilesModel tilesModel : getAllTilesModels(psiElement)) {
          final XmlTag definition = tilesModel.getTileTag(path);
          if (definition != null) {
            return definition;
          }
        }

        return null;
      }
    };
  }

  private static List<TilesModel> getAllTilesModels(@NotNull final PsiElement psiElement) {
    final Module module = ModuleUtil.findModuleForPsiElement(psiElement);
    if (module == null) {
      return Collections.emptyList();
    }
    return StrutsManager.getInstance().getAllTilesModels(module);
  }

  /**
   * Reference to a Tiles definition.
   */
  private static class TilesDefinitionReference extends PsiReferenceBase<XmlElement> implements EmptyResolveMessageProvider {

    private final List<TilesModel> allTilesModels;
    private final String definitionName;

    private static final Function<TilesModel, Collection<? extends Definition>> DEFINITION_COLLECTOR =
        new Function<TilesModel, Collection<? extends Definition>>() {
          public Collection<? extends Definition> fun(final TilesModel tilesModel) {
            return tilesModel.getDefinitions();
          }
        };

    private TilesDefinitionReference(@NotNull final XmlTag xmlElement,
                                     @NotNull final List<TilesModel> allTilesModels) {
      super(xmlElement, true);
      this.allTilesModels = allTilesModels;
      this.definitionName = xmlElement.getValue().getTrimmedText();
    }

    public PsiElement resolve() {
      for (final TilesModel tilesModel : allTilesModels) {
        final XmlTag definition = tilesModel.getTileTag(definitionName);
        if (definition != null) {
          return definition;
        }
      }

      return null;
    }

    @NotNull
    public Object[] getVariants() {
      final List<Definition> definitions = ContainerUtil.concat(allTilesModels, DEFINITION_COLLECTOR);
      return ElementPresentationManager.getInstance().createVariants(definitions);
    }

    public String getUnresolvedMessagePattern() {
      return "Cannot resolve Tiles definition ''" + getValue() + "''";
    }
  }

}