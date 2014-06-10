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

package com.intellij.hibernate.model.annotations.mapping;

import static com.intellij.hibernate.model.HibernateConstants.COLLECTION_OF_ELEMENTS_ANNO;
import static com.intellij.patterns.PsiJavaPatterns.psiMember;
import com.intellij.patterns.PsiMemberPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.semantic.SemContributor;
import com.intellij.semantic.SemRegistrar;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.persistence.util.JavaContainerType;
import com.intellij.persistence.util.PersistenceCommonUtil;
import static com.intellij.persistence.roles.PersistenceClassRoleEnum.EMBEDDABLE;
import com.intellij.openapi.util.Pair;
import com.intellij.jpa.util.JpaUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gregory.Shrago
 */
public class HibernateJamContributor extends SemContributor{

  static {
    JpaJamExtender.extendJpa();
  }

  public void registerSemProviders(final SemRegistrar registrar) {
    CollectionOfElementsImpl.COLLECTION_OF_ELEMENTS_ATTR_META.register(registrar, psiMember().withAnnotation(COLLECTION_OF_ELEMENTS_ANNO).andNot(createHasEmbeddableTargetPattern()));
    CollectionOfElementsImpl.COLLECTION_OF_EMBEDDED_ELEMENTS_ATTR_META.register(registrar, psiMember().withAnnotation(COLLECTION_OF_ELEMENTS_ANNO).and(createHasEmbeddableTargetPattern()));
  }

  private static PsiMemberPattern.Capture createHasEmbeddableTargetPattern() {
    return psiMember().with(new PatternCondition<PsiMember>("hasEmbeddableTarget") {
      @Override
      public boolean accepts(@NotNull final PsiMember psiMember, final ProcessingContext context) {
        final Pair<JavaContainerType, PsiType> pair = PersistenceCommonUtil.getContainerType(PropertyUtil.getPropertyType(psiMember));
        return JpaUtil.isPersistentObject(pair.getSecond(), EMBEDDABLE);
      }
    });
  }


}
