package com.intellij.seam.impl.model.xml;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiType;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.model.xml.SeamDomModel;
import com.intellij.seam.model.xml.components.*;
import com.intellij.util.containers.ConcurrentFactoryMap;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.model.impl.DomModelImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SeamDomModelImpl extends DomModelImpl<SeamComponents> implements SeamDomModel {
  private final Module myModule;


  public SeamDomModelImpl(@NotNull Module module, @NotNull DomFileElement<SeamComponents> mergedModel, @NotNull Set<XmlFile> configFiles) {
    super(mergedModel, configFiles);
    myModule = module;
  }

  private final NotNullLazyValue<List<SeamDomComponent>> myDomComponents = new NotNullLazyValue<List<SeamDomComponent>>() {
    @NotNull
    protected List<SeamDomComponent> compute() {
      final List<SeamDomComponent> seamDomComponents = new ArrayList<SeamDomComponent>();
      for (final DomFileElement<SeamComponents> element : getRoots()) {
        for (SeamDomComponent domComponent : DomUtil.getDefinedChildrenOfType(element.getRootElement(), SeamDomComponent.class)) {
          seamDomComponents.add(domComponent);
        }
      }

      return seamDomComponents;
    }
  };

  private final NotNullLazyValue<List<SeamImport>> myImports = new NotNullLazyValue<List<SeamImport>>() {
    @NotNull
    protected List<SeamImport> compute() {
      final List<SeamImport> seamImports = new ArrayList<SeamImport>();
      for (final DomFileElement<SeamComponents> element : getRoots()) {
        for (SeamImport seamImport : DomUtil.getDefinedChildrenOfType(element.getRootElement(), SeamImport.class)) {
          seamImports.add(seamImport);
        }
      }

      return seamImports;
    }
  };
  private final NotNullLazyValue<List<SeamEvent>> myEventTypes = new NotNullLazyValue<List<SeamEvent>>() {
    @NotNull
    protected List<SeamEvent> compute() {
      final List<SeamEvent> seamEventTypes = new ArrayList<SeamEvent>();
      for (final DomFileElement<SeamComponents> element : getRoots()) {
        for (SeamEvent seamEventType : DomUtil.getDefinedChildrenOfType(element.getRootElement(), SeamEvent.class)) {
          seamEventTypes.add(seamEventType);
        }
      }

      return seamEventTypes;
    }
  };
  private final NotNullLazyValue<List<SeamDomFactory>> myFactories = new NotNullLazyValue<List<SeamDomFactory>>() {
    @NotNull
    protected List<SeamDomFactory> compute() {
      final List<SeamDomFactory> factories = new ArrayList<SeamDomFactory>();
      for (final DomFileElement<SeamComponents> element : getRoots()) {
        for (SeamDomFactory domFactory : DomUtil.getDefinedChildrenOfType(element.getRootElement(), SeamDomFactory.class)) {
          factories.add(domFactory);
        }
      }

      return factories;
    }
  };
  private final ConcurrentFactoryMap<PsiType, List<SeamDomComponent>> myComponentsByClass =
    new ConcurrentFactoryMap<PsiType, List<SeamDomComponent>>() {
      protected List<SeamDomComponent> create(final PsiType key) {
        return computeSeamComponentsByPsiType(key);
      }
    };

  private final ConcurrentFactoryMap<String, List<SeamDomComponent>> myComponentsByName =
    new ConcurrentFactoryMap<String, List<SeamDomComponent>>() {
      protected List<SeamDomComponent> create(final String key) {
        return computeSeamComponentsByName(key);
      }
    };

  private List<SeamDomComponent> computeSeamComponentsByName(@NotNull final String componentName) {
    List<SeamDomComponent> components = new ArrayList<SeamDomComponent>();
    for (SeamDomComponent domComponent : getSeamComponents()) {
      if (componentName.equals(domComponent.getComponentName())) {
        components.add(domComponent);
      }
    }
    return components;
  }

  private List<SeamDomComponent> computeSeamComponentsByPsiType(final PsiType PsiType) {
    List<SeamDomComponent> components = new ArrayList<SeamDomComponent>();
    for (SeamDomComponent domComponent : getSeamComponents()) {
      PsiType componentClass = domComponent.getComponentType();
      if (PsiType.equals(componentClass)) {
        components.add(domComponent);
      }
    }
    return components;
  }

  @NotNull
  public List<SeamDomComponent> getSeamComponents() {
    return myDomComponents.getValue();
  }

  @NotNull
  public List<SeamDomComponent> getSeamComponents(@NotNull final PsiType psiType) {
    return myComponentsByClass.get(psiType);
  }

  @NotNull
  public List<SeamDomComponent> getSeamComponents(@NotNull final String componentName) {
    return myComponentsByName.get(componentName);
  }

  @NotNull
  public List<SeamImport> getImports() {
    return myImports.getValue();
  }

  @NotNull
  public List<SeamDomFactory> getFactories() {
    return myFactories.getValue();
  }

  @NotNull
  public List<SeamEvent> getEvents() {
    return myEventTypes.getValue();
  }

  public Module getModule() {
    return myModule;
  }
}
