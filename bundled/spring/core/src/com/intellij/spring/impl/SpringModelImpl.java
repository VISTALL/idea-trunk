/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.SpringModelVisitor;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.jam.javaConfig.SpringJavaBean;
import com.intellij.spring.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.model.jam.stereotype.SpringStereotypeElement;
import com.intellij.spring.model.jam.utils.SpringJamUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.SpringQualifier;
import com.intellij.spring.model.xml.beans.Alias;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ConcurrentFactoryMap;
import com.intellij.util.containers.ConcurrentHashMap;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.model.impl.DomModelImpl;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Dmitry Avdeev
 */
public class SpringModelImpl extends DomModelImpl<Beans> implements SpringModel {

  private abstract static class Class2BeansMap {
    private final Map<String, List<SpringBaseBeanPointer>> myMap = new HashMap<String, List<SpringBaseBeanPointer>>();

    List<SpringBaseBeanPointer> get(PsiClass psiClass) {
      String fqn = psiClass.getQualifiedName();
      if (fqn == null) {
        return Collections.emptyList();
      }
      List<SpringBaseBeanPointer> pointers = myMap.get(fqn);
      if (pointers == null) {
        pointers = new ArrayList<SpringBaseBeanPointer>();
        compute(psiClass, pointers);
        myMap.put(fqn, pointers);
      }
      return pointers;

    }

    protected abstract void compute(PsiClass psiClass, List<SpringBaseBeanPointer> pointers);
  }

  @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
  private final ConcurrentFactoryMap<SpringQualifier, List<SpringBaseBeanPointer>> myBeansByQualifier =
    new ConcurrentFactoryMap<SpringQualifier, List<SpringBaseBeanPointer>>() {
      protected List<SpringBaseBeanPointer> create(final SpringQualifier key) {
        return computeBeansByQualifier(key);
      }
    };

  @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
  private final ConcurrentFactoryMap<PsiClass, List<SpringBaseBeanPointer>> myBeansByClass =
    new ConcurrentFactoryMap<PsiClass, List<SpringBaseBeanPointer>>() {
      protected List<SpringBaseBeanPointer> create(final PsiClass key) {
        return computeBeansByPsiClass(key);
      }
    };

  private final Class2BeansMap myBeansByEffectiveClassWithInheritance = new Class2BeansMap() {
    @Override
    protected void compute(PsiClass psiClass, List<SpringBaseBeanPointer> pointers) {
      for (final SpringBaseBeanPointer bean : getAllCommonBeans()) {
        for (PsiClass beanClass : bean.getEffectiveBeanType()) {
          if (InheritanceUtil.isInheritorOrSelf(beanClass, psiClass, true)) {
            pointers.add(bean);
          }
        }
      }
    }
  };

  private final AtomicNotNullLazyValue<MultiMap<PsiClass, SpringBaseBeanPointer>> myBeansByClassWithInheritance =
    new AtomicNotNullLazyValue<MultiMap<PsiClass, SpringBaseBeanPointer>>() {
      @NotNull
      @Override
      protected MultiMap<PsiClass, SpringBaseBeanPointer> compute() {
        return computeBeansByPsiClassWithInheritance();
      }
    };

  private final AtomicNotNullLazyValue<MultiMap<String, XmlTag>> myCustomBeanIdCandidates =
    new AtomicNotNullLazyValue<MultiMap<String, XmlTag>>() {
      @NotNull
      @Override
      protected MultiMap<String, XmlTag> compute() {
        final MultiMap<String, XmlTag> map = new MultiMap<String, XmlTag>();
        for (final DomFileElement<Beans> element : getRoots()) {
          for (CustomBeanWrapper bean : DomUtil.getDefinedChildrenOfType(element.getRootElement(), CustomBeanWrapper.class)) {
            if (!bean.isParsed()) {
              final XmlTag tag = bean.getXmlTag();
              for (XmlAttribute attribute : tag.getAttributes()) {
                map.putValue(attribute.getDisplayValue(), tag);
              }
            }
          }
        }

        return map;
      }
    };

  private BeanNamesMapper myBeanNamesMapper;

  @Nullable private final Module myModule;

  private SpringModel[] myDependencies = EMPTY_ARRAY;

  private final SpringFileSet myFileSet;

  private Collection<? extends SpringBaseBeanPointer> myBeansWithoutDependencies;

  private Collection<? extends SpringBaseBeanPointer> myBeans;

  private final AtomicNotNullLazyValue<Collection<SpringBaseBeanPointer>> myOwnBeans =
    new AtomicNotNullLazyValue<Collection<SpringBaseBeanPointer>>() {
      @NotNull
      protected Collection<SpringBaseBeanPointer> compute() {
        Collection<SpringBaseBeanPointer> beans = null;
        for (final DomFileElement<Beans> element : getRoots()) {
          final List<CommonSpringBean> springBeanList = SpringUtils.getChildBeans(element.getRootElement(), false);
          if (beans == null) {
            beans = new ArrayList<SpringBaseBeanPointer>(springBeanList.size());
          }
          for (CommonSpringBean bean : springBeanList) {
            beans.add(SpringBeanPointer.createSpringBeanPointer(bean));
          }
        }
        return beans == null ? Collections.<SpringBaseBeanPointer>emptySet() : beans;
      }
    };

  private final AtomicNotNullLazyValue<MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer>> myDirectInheritorsMap =
    new AtomicNotNullLazyValue<MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer>>() {
      @NotNull
      protected MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> compute() {
        final MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> map = new MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer>() {
          protected Map<SpringBaseBeanPointer, Collection<SpringBaseBeanPointer>> createMap() {
            return new ConcurrentHashMap<SpringBaseBeanPointer, Collection<SpringBaseBeanPointer>>();
          }
        };
        for (final SpringBaseBeanPointer pointer : getAllDomBeans()) {
          final SpringBeanPointer parentPointer = pointer.getParentPointer();
          if (parentPointer != null) {
            map.putValue(parentPointer.getBasePointer(), pointer);
          }
        }
        return map;
      }
    };

  private List<SpringJavaConfiguration> myJavaConfigurations;

  private interface ModelVisitor {


    /**
     * @param model
     * @return false to stop traversing
     */
    boolean visit(SpringModel model);

  }

  public SpringModelImpl(@NotNull final DomFileElement<Beans> mergedModel,
                         @NotNull final Set<XmlFile> configFiles,
                         final Module module,
                         final SpringFileSet fileSet) {

    super(mergedModel, configFiles);
    myFileSet = fileSet;
    myModule = module;
  }

  public List<SpringJavaConfiguration> getJavaConfigurations() {

    return SpringJamUtils.getJavaConfigurations(this);
  }

  private boolean visitDependencies(final ModelVisitor visitor) {
    for (SpringModel dependency : myDependencies) {
      if (!visitor.visit(dependency)) {
        return false;
      }
      if (dependency instanceof SpringModelImpl) {
        ((SpringModelImpl)dependency).visitDependencies(visitor);
      }
    }
    return true;
  }

  List<Alias> getAliases(boolean withDeps) {
    final ArrayList<Alias> list = new ArrayList<Alias>();
    final ModelVisitor modelVisitor = new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        for (DomFileElement<Beans> fileElement : model.getRoots()) {
          list.addAll(fileElement.getRootElement().getAliases());
        }
        return true;
      }
    };
    modelVisitor.visit(this);
    if (withDeps) {
      visitDependencies(modelVisitor);
    }
    return list;
  }

  @NotNull
  public String getId() {
    return myFileSet.getId();
  }

  public SpringFileSet getFileSet() {
    return myFileSet;
  }

  @NotNull
  public SpringModel[] getDependencies() {
    return myDependencies == null ? EMPTY_ARRAY : myDependencies;
  }

  public Collection<XmlTag> getCustomBeanCandidates(String id) {
    return myCustomBeanIdCandidates.getValue().get(id);
  }

  public void setDependencies(@NotNull final SpringModel[] dependencies) {
    myDependencies = dependencies;
  }

  @Nullable
  public SpringBeanPointer findBean(@NonNls @NotNull String beanName) {
    return getBeanNamesMapper().getBean(beanName);
  }

  @Nullable
  public SpringBeanPointer findParentBean(@NonNls @NotNull final String beanName) {
    for (SpringModel dependency : myDependencies) {
      final SpringBeanPointer springBean = dependency.findBean(beanName);
      if (springBean != null) {
        return springBean;
      }
    }
    return null;
  }

  private BeanNamesMapper getBeanNamesMapper() {
    if (myBeanNamesMapper == null) myBeanNamesMapper = new BeanNamesMapper(this);
    return myBeanNamesMapper;
  }

  @NotNull
  public Collection<SpringBaseBeanPointer> getAllDomBeans() {
    return getAllDomBeans(true);
  }

  public Collection<SpringBaseBeanPointer> getOwnBeans() {
    return myOwnBeans.getValue();
  }

  @NotNull
  public Collection<SpringBaseBeanPointer> getAllDomBeans(boolean withDependencies) {

    final Collection<SpringBaseBeanPointer> ownBeans = getOwnBeans();
    if (withDependencies) {
      final List<SpringBaseBeanPointer> allBeans = new ArrayList<SpringBaseBeanPointer>(ownBeans);
      visitDependencies(new ModelVisitor() {
        public boolean visit(final SpringModel model) {
          allBeans.addAll(model.getOwnBeans());
          return true;
        }
      });
      return allBeans;
    }
    else {
      return ownBeans;
    }
  }

  @NotNull
  public Set<String> getAllBeanNames(@NotNull final String beanName) {
    return getBeanNamesMapper().getAllBeanNames(beanName);
  }

  public boolean isNameDuplicated(@NotNull final String beanName) {
    return getBeanNamesMapper().isNameDuplicated(beanName);
  }

  @NotNull
  public synchronized Collection<? extends SpringBaseBeanPointer> getAllCommonBeans(final boolean withDepenedencies) {
    if (!withDepenedencies || myDependencies.length == 0) {
      return myBeansWithoutDependencies == null
             ? myBeansWithoutDependencies = calculateBeans(withDepenedencies)
             : myBeansWithoutDependencies;
    }
    else {
      return myBeans == null ? myBeans = calculateBeans(withDepenedencies) : myBeans;
    }
  }

  private Collection<SpringBaseBeanPointer> calculateBeans(final boolean withDepenedencies) {
    Collection<SpringBaseBeanPointer> domBeans = getAllDomBeans(withDepenedencies);
    final Collection<SpringBaseBeanPointer> allBeans = new ArrayList<SpringBaseBeanPointer>(domBeans);

    processNonDomBeans(new Consumer<CommonSpringBean>() {
      public void consume(final CommonSpringBean bean) {
        allBeans.add(SpringBeanPointer.createSpringBeanPointer(bean));
      }
    });

    return allBeans;
  }

  private void processNonDomBeans(final Consumer<CommonSpringBean> consumer) {
    for (SpringJavaConfiguration javaConfiguration : getJavaConfigurations()) {
      for (SpringJavaBean javaBean : javaConfiguration.getBeans()) {
        if (javaBean.isPublic()) {
          consumer.consume(javaBean);
        }
      }
    }
    for (final SpringStereotypeElement element : SpringJamUtils.getAllStereotypeJavaBeans(this)) {
      consumer.consume(element);
    }
  }

  @NotNull
  public Collection<? extends SpringBaseBeanPointer> getAllCommonBeans() {
    return getAllCommonBeans(true);
  }

  @NotNull
  public Collection<? extends SpringBaseBeanPointer> getAllParentBeans() {
    final Collection<SpringBaseBeanPointer> allBeans = new ArrayList<SpringBaseBeanPointer>();

    visitDependencies(new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        allBeans.addAll(model.getAllCommonBeans());
        return true;
      }
    });

    return allBeans;
  }

  public List<SpringBaseBeanPointer> findQualifiedBeans(@NotNull final SpringQualifier qualifier) {
    final List<SpringBaseBeanPointer> pointers = new ArrayList<SpringBaseBeanPointer>(myBeansByQualifier.get(qualifier));
    visitDependencies(new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        pointers.addAll(((SpringModelImpl)model).myBeansByQualifier.get(qualifier));
        return true;
      }
    });
    return pointers;
  }

  private List<SpringBaseBeanPointer> computeBeansByQualifier(final SpringQualifier pair) {
    final List<SpringBaseBeanPointer> beans = new ArrayList<SpringBaseBeanPointer>();
    final Collection<? extends SpringBaseBeanPointer> pointers = getAllCommonBeans(true);
    for (SpringBaseBeanPointer beanPointer : pointers) {
      final CommonSpringBean bean = beanPointer.getSpringBean();
      final SpringQualifier qualifier = bean.getSpringQualifier();
      if (qualifier != null) {
        if (SpringUtils.compareQualifiers(qualifier, pair)) {
          beans.add(beanPointer);
        }
      }
    }
    return beans;
  }


  @NotNull
  public List<SpringBaseBeanPointer> findBeansByPsiClass(@NotNull final PsiClass psiClass) {
    final List<SpringBaseBeanPointer> pointers = new ArrayList<SpringBaseBeanPointer>(myBeansByClass.get(psiClass));
    visitDependencies(new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        pointers.addAll(((SpringModelImpl)model).myBeansByClass.get(psiClass));
        return true;
      }
    });
    return pointers;
  }

  @NotNull
  public List<SpringBaseBeanPointer> findBeansByPsiClassWithInheritance(@NotNull final PsiClass psiClass) {
    final ArrayList<SpringBaseBeanPointer> pointers =
      new ArrayList<SpringBaseBeanPointer>(myBeansByClassWithInheritance.getValue().get(psiClass));
    visitDependencies(new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        pointers.addAll(((SpringModelImpl)model).myBeansByClassWithInheritance.getValue().get(psiClass));
        return true;
      }
    });

    return pointers;
  }

  @NotNull
  public List<SpringBaseBeanPointer> findBeansByEffectivePsiClassWithInheritance(@NotNull final PsiClass psiClass) {
    return collectBeans(psiClass, new Function<SpringModelImpl, Class2BeansMap>() {
      public Class2BeansMap fun(SpringModelImpl springModel) {
        return springModel.myBeansByEffectiveClassWithInheritance;
      }
    });
  }

  private List<SpringBaseBeanPointer> collectBeans(final PsiClass psiClass, final Function<SpringModelImpl, Class2BeansMap> getter) {
    final ArrayList<SpringBaseBeanPointer> pointers = new ArrayList<SpringBaseBeanPointer>(getter.fun(this).get(psiClass));
    visitDependencies(new ModelVisitor() {
      public boolean visit(final SpringModel model) {
        pointers.addAll(getter.fun((SpringModelImpl)model).get(psiClass));
        return true;
      }
    });

    return pointers;
  }

  @NotNull
  public List<SpringBaseBeanPointer> getChildren(@NotNull SpringBeanPointer parent) {
    final SpringBaseBeanPointer baseParent = parent.getBasePointer();
    final ArrayList<SpringBaseBeanPointer> list = new ArrayList<SpringBaseBeanPointer>();
    for (SpringBaseBeanPointer bean : getAllDomBeans()) {
      final SpringBeanPointer pointer = bean.getParentPointer();
      if (pointer != null && pointer.getBasePointer().equals(baseParent)) {
        list.add(bean);
      }
    }
    return list;
  }

  private static void addDescendants(MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> map,
                                     SpringBaseBeanPointer current,
                                     Set<SpringBaseBeanPointer> result) {
    final Collection<SpringBaseBeanPointer> pointers = map.get(current);
    for (final SpringBaseBeanPointer pointer : pointers) {
      if (result.add(pointer)) {
        addDescendants(map, pointer, result);
      }
    }
  }

  @NotNull
  public List<SpringBaseBeanPointer> getDescendants(final @NotNull CommonSpringBean context) {
    final Set<SpringBaseBeanPointer> visited = new THashSet<SpringBaseBeanPointer>();
    final SpringBaseBeanPointer pointer = SpringBeanPointer.createSpringBeanPointer(context);
    visited.add(pointer);
    final MultiMap<SpringBaseBeanPointer, SpringBaseBeanPointer> map = myDirectInheritorsMap.getValue();
    addDescendants(map, pointer, visited);
    return new SmartList<SpringBaseBeanPointer>(visited);
  }

  private List<SpringBaseBeanPointer> computeBeansByPsiClass(@NotNull final PsiClass psiClass) {
    final List<SpringBaseBeanPointer> beans = new ArrayList<SpringBaseBeanPointer>();
    final Consumer<CommonSpringBean> consumer = new Consumer<CommonSpringBean>() {
      public void consume(final CommonSpringBean bean) {
        final PsiClass beanClass = bean.getBeanClass();
        if (beanClass != null && beanClass.equals(psiClass)) {
          beans.add(SpringBeanPointer.createSpringBeanPointer(bean));
        }
      }
    };

    processAllBeans(consumer);

    return beans;
  }

  private void processAllBeans(final Consumer<CommonSpringBean> consumer) {
    final SpringModelVisitor visitor = new SpringModelVisitor() {
      public boolean visitBean(final CommonSpringBean bean) {
        consumer.consume(bean);
        return true;
      }
    };
    for (final DomFileElement<Beans> element : getRoots()) {
      SpringModelVisitor.visitBeans(visitor, element.getRootElement());
    }
    processNonDomBeans(consumer);
  }

  private MultiMap<PsiClass, SpringBaseBeanPointer> computeBeansByPsiClassWithInheritance() {
    final MultiMap<PsiClass, SpringBaseBeanPointer> result = new MultiMap<PsiClass, SpringBaseBeanPointer>();
    final Consumer<CommonSpringBean> consumer = new Consumer<CommonSpringBean>() {
      public void consume(final CommonSpringBean bean) {
        final PsiClass beanClass = bean.getBeanClass();
        if (beanClass == null) return;

        final SpringBaseBeanPointer pointer = SpringBeanPointer.createSpringBeanPointer(bean);
        InheritanceUtil.processSupers(beanClass, true, new Processor<PsiClass>() {
          public boolean process(final PsiClass psiClass) {
            result.putValue(psiClass, pointer);
            return true;
          }
        });
      }
    };

    processAllBeans(consumer);

    return result;
  }

  @Nullable
  public Module getModule() {
    return myModule;
  }

  public String toString() {
    return getId();
  }
}
