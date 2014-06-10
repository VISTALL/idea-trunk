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
import com.intellij.jam.reflect.JamMethodMeta;
import com.intellij.psi.PsiRef;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.pom.PomTarget;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;

import java.util.List;

public abstract class JavaSpringConfiguration extends SpringJavaConfiguration {
  public static final JamClassMeta<JavaSpringConfiguration> META = new JamClassMeta<JavaSpringConfiguration>(JavaSpringConfiguration.class);

  public static final JamMethodMeta<JavaSpringJavaBean> BEANS_METHOD_META =
    new JamMethodMeta<JavaSpringJavaBean>(JavaSpringJavaBean.class).addAnnotation(JavaSpringJavaBean.META);

  static {
    BEANS_METHOD_META.addPomTargetProducer(new PairConsumer<JavaSpringJavaBean, Consumer<PomTarget>>() {
      public void consume(JavaSpringJavaBean javaSpringJavaBean, Consumer<PomTarget> pomTargetConsumer) {
        for (PomTarget pomTarget : javaSpringJavaBean.getPomTargets()) {
          pomTargetConsumer.consume(pomTarget);
        }
      }
    });
  }

  private static final JamChildrenQuery<JavaSpringJavaBean> BEANS_QUERY =
    JamChildrenQuery.annotatedMethods(JavaSpringJavaBean.META, BEANS_METHOD_META);

  public JavaSpringConfiguration() {
    super(SpringAnnotationsConstants.JAVA_SPRING_CONFIGURATION_ANNOTATION);
  }

  static {
    META.addChildrenQuery(BEANS_QUERY);
  }

  public List<? extends SpringJavaBean> getBeans() {
    return BEANS_QUERY.findChildren(PsiRef.real(getPsiElement()));
  }
}