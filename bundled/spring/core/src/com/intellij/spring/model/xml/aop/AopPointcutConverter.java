/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.aop;

import com.intellij.aop.AopPointcut;
import com.intellij.aop.jam.AopModuleService;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class AopPointcutConverter extends ResolvingConverter<AopPointcut> {
  @NotNull
  public Collection<? extends AopPointcut> getVariants(final ConvertContext context) {
    final XmlElement element = context.getXmlElement();
    if (element == null) return Collections.emptyList();

    return AopModuleService.getAopModel(ModuleUtil.findModuleForPsiElement(element)).getPointcuts();
  }

  public AopPointcut fromString(@Nullable @NonNls final String s, final ConvertContext context) {
    return s == null ? null : ContainerUtil.find(getVariants(context), new Condition<AopPointcut>() {
      public boolean value(final AopPointcut o) {
        return s.equals(o.getQualifiedName().getStringValue());
      }
    });
  }

  public String toString(@Nullable AopPointcut aopPointcut, final ConvertContext context) {
    return aopPointcut == null ? null : aopPointcut.getQualifiedName().getStringValue();
  }
}
