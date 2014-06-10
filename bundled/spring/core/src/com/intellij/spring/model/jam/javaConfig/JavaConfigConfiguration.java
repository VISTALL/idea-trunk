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

package com.intellij.spring.model.jam.javaConfig;

import com.intellij.jam.reflect.JamChildrenQuery;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.psi.PsiRef;
import com.intellij.spring.constants.SpringAnnotationsConstants;

import java.util.List;

public abstract class JavaConfigConfiguration extends SpringJavaConfiguration {
  public static final JamClassMeta<JavaConfigConfiguration> META = new JamClassMeta<JavaConfigConfiguration>(JavaConfigConfiguration.class);

  private static final JamChildrenQuery<JavaConfigJavaBean> BEANS_QUERY =
    JamChildrenQuery.annotatedMethods(JavaConfigJavaBean.META, JavaConfigJavaBean.class);

  private static final JamChildrenQuery<SpringJavaExternalBean> EXTERNAL_BEANS_QUERY =
    JamChildrenQuery.annotatedMethods(SpringJavaExternalBean.META, SpringJavaExternalBean.class);

  public JavaConfigConfiguration() {
    super(SpringAnnotationsConstants.JAVA_CONFIG_CONFIGURATION_ANNOTATION);
  }

  static {
    META.addChildrenQuery(BEANS_QUERY);
    META.addChildrenQuery(EXTERNAL_BEANS_QUERY);
  }

  public List<? extends SpringJavaBean> getBeans() {
    return BEANS_QUERY.findChildren(PsiRef.real(getPsiElement()));
  }

  public List<? extends SpringJavaExternalBean> getExternalBeans() {
    return EXTERNAL_BEANS_QUERY.findChildren(PsiRef.real(getPsiElement()));
  }
}