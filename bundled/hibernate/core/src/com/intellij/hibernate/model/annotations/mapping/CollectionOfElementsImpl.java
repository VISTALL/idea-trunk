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

import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.hibernate.model.common.mapping.CollectionOfElements;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamMemberMeta;
import com.intellij.javaee.model.common.persistence.mapping.Embedded;
import com.intellij.jpa.model.annotations.mapping.AttributeBaseImpl;
import com.intellij.psi.PsiMember;

/**
 * @author Gregory.Shrago
 */
public class CollectionOfElementsImpl extends AttributeBaseImpl implements CollectionOfElements {
  public static final JamAnnotationMeta COLLECTION_OF_ELEMENTS_ANNO_META = new JamAnnotationMeta(HibernateConstants.COLLECTION_OF_ELEMENTS_ANNO);

  public static final JamMemberMeta<PsiMember, CollectionOfElementsImpl> COLLECTION_OF_ELEMENTS_ATTR_META = new JamMemberMeta<PsiMember, CollectionOfElementsImpl>(ATTRIBUTE_ARCHETYPE, CollectionOfElementsImpl.class).addAnnotation(COLLECTION_OF_ELEMENTS_ANNO_META);
  public static final JamMemberMeta<PsiMember, EmbeddedImpl> COLLECTION_OF_EMBEDDED_ELEMENTS_ATTR_META = new JamMemberMeta<PsiMember, EmbeddedImpl>(
    COLLECTION_OF_ELEMENTS_ATTR_META, EmbeddedImpl.class).addAnnotation(COLLECTION_OF_ELEMENTS_ANNO_META);

  public static class EmbeddedImpl extends CollectionOfElementsImpl implements Embedded {

    public EmbeddedImpl(final PsiMember psiMember) {
      super(psiMember);
    }
  }

  public CollectionOfElementsImpl(final PsiMember psiMember) {
    super(psiMember);
  }

  public JamAnnotationMeta getMeta() {
    return COLLECTION_OF_ELEMENTS_ANNO_META;
  }
}
