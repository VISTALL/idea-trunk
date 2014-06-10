package com.intellij.seam.model.jam;

import com.intellij.jam.JamElement;
import com.intellij.jam.JamService;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.seam.constants.SeamAnnotationConstants;
import com.intellij.seam.model.xml.SeamDomModel;
import com.intellij.seam.model.xml.SeamDomModelManager;
import com.intellij.seam.model.xml.components.SeamDomComponent;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeamJamModel {
  private static final Key<CachedValue<MergedSeamComponent>> MERGED_SEAM_COMPONENT = Key.create("MERGED_SEAM_COMPONENT");

  private final Module myModule;

  public static SeamJamModel getModel(@NotNull Module module) {
    return ModuleServiceManager.getService(module, SeamJamModel.class);
  }

  public SeamJamModel(@NotNull final Module module) {
    myModule = module;
  }

  public Module getModule() {
    return myModule;
  }

  public List<SeamJamComponent> getAnnotatedSeamComponents(boolean showFromLibraries) {
    return getJamClassElements(SeamJamComponent.META, SeamAnnotationConstants.COMPONENT_ANNOTATION, showFromLibraries);
  }

  public <T extends JamElement> List<T> getJamClassElements(final JamClassMeta<T> clazz, final String anno, boolean showFromLibraries) {
    final JamService service = JamService.getJamService(myModule.getProject());

    final GlobalSearchScope scope = getScope(showFromLibraries);

    return service.getJamClassElements(clazz, anno, scope);
  }

  private GlobalSearchScope getScope(boolean showFromLibraries) {
    return showFromLibraries
           ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule)
           : GlobalSearchScope.moduleWithDependenciesScope(myModule);
  }

  @NotNull
  public Set<SeamJamComponent> getSeamComponents() {
    return getSeamComponents(true);
  }

  @NotNull
  public Set<SeamJamComponent> getSeamComponents(final boolean mergeDomComponents) {
    return getSeamComponents(mergeDomComponents, true);
  }

  @NotNull
  public Set<SeamJamComponent> getSeamComponents(final boolean mergeDomComponents, boolean fromLibs) {
    Set<SeamJamComponent> components = new HashSet<SeamJamComponent>();

    final List<SeamJamComponent> annotated = getAnnotatedSeamComponents(fromLibs);
    components.addAll(annotated);
    if (mergeDomComponents) {
      components.addAll(getMergedComponents(annotated));
    }

    return components;
  }

  public List<SeamJamComponent> getMergedComponents(boolean fromLibs) {
    return getMergedComponents(getAnnotatedSeamComponents(fromLibs));
  }

  public List<SeamJamComponent> getMergedComponents(List<SeamJamComponent> annotated) {
    List<SeamJamComponent> mergedComponents = new ArrayList<SeamJamComponent>();
    final Module module = getModule();
    if (module != null) {
      final List<PsiType> psiTypes = ContainerUtil.mapNotNull(annotated, new Function<SeamJamComponent, PsiType>() {
        public PsiType fun(final SeamJamComponent seamJamComponent) {
          return seamJamComponent.getComponentType();
        }
      });

      for (SeamDomModel model : SeamDomModelManager.getInstance(module.getProject()).getAllModels(module)) {
        for (final SeamDomComponent domComponent : model.getSeamComponents()) {
          if (!psiTypes.contains(domComponent.getComponentType())) {
            final PsiType type = domComponent.getComponentType();
            if (type instanceof PsiClassType) {
              final PsiClass psiClass = ((PsiClassType)type).resolve();
              if (psiClass != null) {
                mergedComponents.add(getOrCreateMergedComponent(psiClass, domComponent));
              }
            }
          }
        }
      }
    }
    return mergedComponents;
  }

  @NotNull
  private MergedSeamComponent getOrCreateMergedComponent(final PsiClass psiClass, final SeamDomComponent domComponent) {
    CachedValue<MergedSeamComponent> cachedValue = psiClass.getUserData(MERGED_SEAM_COMPONENT);

    if (cachedValue == null) {
      cachedValue = psiClass.getManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<MergedSeamComponent>() {
        public Result<MergedSeamComponent> compute() {
          MergedSeamComponent mergedSeamComponent = new MergedSeamComponent(psiClass, domComponent);

          return new Result<MergedSeamComponent>(mergedSeamComponent, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
        }
      });

      psiClass.putUserData(MERGED_SEAM_COMPONENT, cachedValue);
    }
    return cachedValue.getValue();
  }
}