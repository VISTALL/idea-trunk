/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.model.enums.LazyType;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import com.intellij.util.xml.DomElement;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Gregory.Shrago
 */
public class LazyTypeConverter extends ResolvingConverter<LazyType> {

  public static final Map<Class, Collection<LazyType>> ourMapping;

  static {
    ourMapping = new THashMap<Class, Collection<LazyType>>();
    final List<LazyType> trueFalse = Arrays.asList(LazyType.TRUE, LazyType.FALSE);
    final List<LazyType> trueFalseExtra = Arrays.asList(LazyType.TRUE, LazyType.FALSE, LazyType.EXTRA);
    final List<LazyType> falseProxyNoproxy = Arrays.asList(LazyType.FALSE, LazyType.PROXY, LazyType.NO_PROXY);
    final List<LazyType> falseProxy = Arrays.asList(LazyType.FALSE, LazyType.PROXY);
    ourMapping.put(HbmAny.class, trueFalse);
    ourMapping.put(HbmBag.class, trueFalseExtra);
    ourMapping.put(HbmClass.class, trueFalse);
    ourMapping.put(HbmComponent.class, trueFalse);
    ourMapping.put(HbmIdbag.class, trueFalseExtra);
    ourMapping.put(HbmJoinedSubclass.class, trueFalse);
    ourMapping.put(HbmKeyManyToOne.class, falseProxy);
    ourMapping.put(HbmList.class, trueFalseExtra);
    ourMapping.put(HbmManyToMany.class, falseProxy);
    ourMapping.put(HbmManyToOne.class, falseProxyNoproxy);
    ourMapping.put(HbmMap.class, trueFalseExtra);
    ourMapping.put(HbmOneToOne.class, falseProxyNoproxy);
    ourMapping.put(HbmProperty.class, trueFalse);
    ourMapping.put(HbmSet.class, trueFalseExtra);
    ourMapping.put(HbmSubclass.class, trueFalse);
    ourMapping.put(HbmUnionSubclass.class, trueFalse);
  }

  @NotNull
  public Collection<LazyType> getVariants(final ConvertContext context) {
    final DomElement parent = context.getInvocationElement().getParent();
    final Class parentClass = parent == null? null : parent.getClass().getInterfaces()[0];
    final Collection<LazyType> types = parentClass == null? null : ourMapping.get(parentClass);
    return types == null? Collections.<LazyType>emptyList() : types;
  }

  public LazyType fromString(@Nullable @NonNls String s, final ConvertContext context) {
    if (s == null) return null;
    final LazyType lazyType = getLazyType(s);
    if (lazyType == null || !getVariants(context).contains(lazyType)) return null;
    return lazyType;
  }

  public String toString(@Nullable LazyType lazyType, final ConvertContext context) {
    return lazyType == null? null : lazyType.getValue();
  }

  @Nullable
  private static LazyType getLazyType(final String s) {
    for (LazyType type : LazyType.values()) {
      if (type.getValue().equals(s)) {
        return type;
      }
    }
    return null;
  }

}