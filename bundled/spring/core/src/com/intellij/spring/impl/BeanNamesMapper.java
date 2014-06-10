/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.spring.model.xml.beans.Alias;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.containers.HashSet;
import com.intellij.util.containers.MultiMap;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Dmitry Avdeev
 */
class BeanNamesMapper {

  private final Map<String, SpringBaseBeanPointer> myBeansMap;
  private final Map<String, String> myAliasesMap;
  private final MultiMap<String, String> myAllBeanNames;

  private final Set<String> myDuplicatedNames = new HashSet<String>();

  public BeanNamesMapper(SpringModelImpl model) {

    final Collection<? extends SpringBaseBeanPointer> springBeans = model.getAllCommonBeans();
    final Collection<SpringBaseBeanPointer> ownBeans = model.getOwnBeans();

    myBeansMap = new THashMap<String, SpringBaseBeanPointer>(springBeans.size());
    myAliasesMap = new THashMap<String, String>();
    myAllBeanNames = new MultiMap<String, String>() {
      @Override
      protected Collection<String> createCollection() {
        return new HashSet<String>();
      }

      @Override
      protected Collection<String> createEmptyCollection() {
        return Collections.emptySet();
      }
    };

    for (final SpringBaseBeanPointer bean : springBeans) {
      final String beanName = bean.getName();
      if (StringUtil.isNotEmpty(beanName)) {
        final SpringBaseBeanPointer duplication = myBeansMap.put(beanName, bean);
        if (duplication != null && ownBeans.contains(duplication)) {
          if (ownBeans.contains(bean)) {
            myDuplicatedNames.add(beanName);
          } else {
            myBeansMap.put(beanName, duplication);
          }
        }
        myAllBeanNames.putValue(beanName, beanName);
        for (final String alias : bean.getAliases()) {
          registerAlias(beanName, alias);
        }
      }
    }

    final List<Alias> aliases = model.getAliases(true);
    for (Alias anAlias : aliases) {
      registerAlias(anAlias.getAliasedBean().getStringValue(), anAlias.getAlias().getStringValue());
    }
  }

  @Nullable
  public SpringBeanPointer getBean(@NotNull final String beanName) {
    String curName = beanName;
    Set<String> visited = null;
    while (true) {
      SpringBeanPointer bean = myBeansMap.get(curName);
      if (bean != null) return bean.derive(beanName);

      final String newName = myAliasesMap.get(curName);
      if (newName == null || (visited != null && visited.contains(curName))) return null;

      if (visited == null) {
        visited = new THashSet<String>();
      }
      visited.add(curName);
      curName = newName;
    }
  }

  public Set<String> getAllBeanNames(String beanName) {
    return (Set<String>)myAllBeanNames.get(beanName); 
  }

  boolean isNameDuplicated(@NotNull String name) {
    return myDuplicatedNames.contains(name);
  }

  private void registerAlias(String beanName, final String alias) {
    
    if (!StringUtil.isNotEmpty(alias) || !StringUtil.isNotEmpty(beanName) || Comparing.equal(beanName, alias)) {
      return;
    }

    final String duplication = myAliasesMap.put(alias, beanName);
    if (duplication != null || myBeansMap.containsKey(alias)) {
      myDuplicatedNames.add(alias);
    }

    final HashSet<String> aliases = new HashSet<String>();
    aliases.add(alias);
    while(!myBeansMap.containsKey(beanName)) {
      beanName = myAliasesMap.get(beanName);
      if (beanName == null) {
        return;
      }
      aliases.add(beanName);
    }
    for (String s : aliases) {
      myAllBeanNames.putValue(beanName, s);
    }
  }

}
