/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl.model.beans;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.spring.impl.model.AbstractDomSpringBean;
import com.intellij.spring.impl.model.DomSpringBeanImpl;
import com.intellij.spring.model.ResolvedConstructorArgs;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.highlighting.SpringConstructorArgResolveUtil;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.SpringQualifier;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.Function;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.DomUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import java.util.*;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class SpringBeanImpl extends DomSpringBeanImpl implements SpringBean {

  private CachedValue<ResolvedConstructorArgsImpl> myResolvedConstructorArgs;
  public static final Function<SpringBean,Collection<SpringPropertyDefinition>> PROPERTIES_GETTER = new Function<SpringBean, Collection<SpringPropertyDefinition>>() {
    public Collection<SpringPropertyDefinition> fun(final SpringBean springBean) {
      return SpringUtils.getProperties(springBean);
    }
  };
  public static final Function<SpringBean,Collection<ConstructorArg>> CTOR_ARGS_GETTER = new Function<SpringBean, Collection<ConstructorArg>>() {
    public Collection<ConstructorArg> fun(final SpringBean springBean) {
      return SpringUtils.getConstructorArgs(springBean);
    }
  };

  /**
   * Returns bean name (id or the first name from "name" attribute).
   *
   * @return bean name (id or the first name from "name" attribute).
   */
  @Nullable
  public String getBeanName() {
    final String id = getId().getStringValue();
    if (id != null) {
      return id;
    }
    else {
      final String name = getName().getStringValue();
      if (name != null) {
        final List<String> list = SpringUtils.tokenize(name);
        return list.size() > 0 ? list.get(0) : null;
      }
    }
    return getClazz().getStringValue();
  }

  @Nullable
  public String getClassName() {
    return getClazz().getStringValue();
  }

  @NotNull
  public String[] getAliases() {
    final String name = getName().getStringValue();
    if (name != null) {
      final List<String> list = SpringUtils.tokenize(name);
      final String id = getId().getStringValue();
      if (id == null && list.size() > 1) {
        list.remove(0);
      }
      return ArrayUtil.toStringArray(list);
    }
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  public void setName(@NotNull String newName) {
    final String id = getId().getStringValue();
    if (id != null) {
      getId().setStringValue(newName);
    }
    else {
      final String name = getName().getStringValue();
      if (name != null) {
        final int first = StringUtil.findFirst(name, SpringUtils.ourFilter);
        if (first >= 0) {
          String newValue = newName + name.substring(first);
          getName().setStringValue(newValue);
        } else {
          getName().setStringValue(newName);          
        }
      }
      else {
        getId().setValue(newName);
      }
    }
  }

  @NotNull
  public abstract GenericAttributeValue<SpringBeanPointer> getFactoryBean();

  @NotNull
  public abstract GenericAttributeValue<PsiMethod> getFactoryMethod();

  @Nullable
  public PsiClass getBeanClass(@Nullable Set<AbstractDomSpringBean> visited, final boolean considerFactories) {
    if (visited != null && visited.contains(this)) return null;

    final PsiClass psiClass = super.getBeanClass(visited, considerFactories);
    if (psiClass != null) return psiClass;

    final SpringBeanPointer parent = getParentBean().getValue();
    if (parent != null) {
      if (visited == null) {
        visited = new THashSet<AbstractDomSpringBean>();
      }
      visited.add(this);
      final CommonSpringBean bean = parent.getSpringBean();
      if (bean instanceof DomSpringBeanImpl) {
        return ((DomSpringBeanImpl)bean).getBeanClass(visited, considerFactories);
      }
    }
    return null;
  }

  public List<SpringPropertyDefinition> getAllProperties() {
    final Set<SpringPropertyDefinition> list = SpringUtils.getMergedSet(this, PROPERTIES_GETTER);
    return new ArrayList<SpringPropertyDefinition>(list);
  }

  public Set<ConstructorArg> getAllConstructorArgs() {
    return SpringUtils.getMergedSet(this, CTOR_ARGS_GETTER);
  }

  @NotNull
  public ResolvedConstructorArgs getResolvedConstructorArgs() {
    if (myResolvedConstructorArgs == null) {
      final CachedValuesManager cachedValuesManager = PsiManager.getInstance(getManager().getProject()).getCachedValuesManager();
      myResolvedConstructorArgs = cachedValuesManager.createCachedValue(new CachedValueProvider<ResolvedConstructorArgsImpl>() {
        public Result<ResolvedConstructorArgsImpl> compute() {
          return Result.createSingleDependency(new ResolvedConstructorArgsImpl(SpringBeanImpl.this),
                                               PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
        }
      }, false);
    }
    final ResolvedConstructorArgs resolvedConstructorArgs = myResolvedConstructorArgs.getValue();
    assert resolvedConstructorArgs != null;
    return resolvedConstructorArgs;
  }

  public boolean isAbstract() {
    final Boolean value = getAbstract().getValue();
    return value != null && value.booleanValue();
  }

  public Autowire getBeanAutowire() {
    Autowire autowire = getAutowire().getValue();
    if (autowire == null) {
      final Beans beans = getParentOfType(Beans.class, false);
      assert beans != null;
      autowire = Autowire.fromDefault(beans.getDefaultAutowire().getValue());
    }
    if (autowire == Autowire.AUTODETECT) {
      return SpringConstructorArgResolveUtil.hasEmptyConstructor(this) ? Autowire.BY_TYPE : Autowire.CONSTRUCTOR;
    }
    else {
      return autowire;
    }
  }

  @NonNls
  public String toString() {
    final String beanName = getBeanName();
    return beanName == null ? "Unknown" : beanName;
  }

  public SpringQualifier getSpringQualifier() {
    final SpringDomQualifier qualifier = getQualifier();
    return !DomUtil.hasXml(qualifier) ? null : qualifier;
  }
}
