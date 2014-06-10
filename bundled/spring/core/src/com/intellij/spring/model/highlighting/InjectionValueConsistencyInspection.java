/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringModelVisitor;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.highlighting.AddDomElementQuickFix;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.RemoveDomElementQuickFix;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class InjectionValueConsistencyInspection extends SpringBeanInspectionBase {

  @NonNls private static final String PROPERTY = SpringBundle.message("spring.bean.property");
  @NonNls private static final String ARG = SpringBundle.message("spring.bean.constructor.arg");
  @NonNls private static final String ENTRY = SpringBundle.message("spring.bean.map.entry");

  protected SpringModelVisitor createVisitor(final DomElementAnnotationHolder holder, final Beans beans, final SpringModel model) {
    return new SpringModelVisitor() {
      protected boolean visitProperty(SpringPropertyDefinition property) {
        if (property instanceof SpringValueHolder) {
          checkValueHolder((SpringValueHolder)property, holder, PROPERTY);
        }
        return true;
      }

      protected boolean visitConstructorArg(ConstructorArg arg) {
        checkValueHolder(arg, holder, ARG);
        return true;
      }

      protected boolean visitMapEntry(final SpringEntry entry) {
        checkMapEntry(holder, entry);
        return true;
      }

      protected boolean visitRef(final SpringRef ref) {
        checkRef(ref, holder);
        return true;
      }

      protected boolean visitIdref(final Idref idref) {
        checkIdref(idref, holder);
        return true;
      }
    };
  }

  private static void checkValueHolder(SpringValueHolder valueHolder, DomElementAnnotationHolder holder, String elementName) {

    final boolean hasRefAttribute = valueHolder.getRefAttr().getXmlAttribute() != null;
    final boolean hasValueAttribute = valueHolder.getValueAttr().getXmlAttribute() != null;

    final Set<DomElement> values = getValues(valueHolder);

    if (!hasRefAttribute && !hasValueAttribute && values.size() == 0) {
      reportNoValue(valueHolder, holder, elementName);
    }
    else if ((hasRefAttribute && hasValueAttribute) || ((hasRefAttribute || hasValueAttribute)) && values.size() > 0) {
      final String message =
        SpringBundle.message("spring.bean.property.value.inconsistency.ref.or.value.subelemet.must.defined", elementName);
      if (hasValueAttribute) {
        reportAttribute(valueHolder.getValueAttr(), holder, message);
      }
      if (hasRefAttribute) {
        reportAttribute(valueHolder.getRefAttr(), holder, message);
      }
      reportSubtags(values, holder, message);
    }
    else if (values.size() > 1) {
      final String message = SpringBundle.message("spring.bean.property.value.inconsistency.more.one.subelement", elementName);
      reportSubtags(values, holder, message);
    }
  }

  private static void checkMapEntry(final DomElementAnnotationHolder holder, final SpringEntry entry) {
    final boolean hasKeyAttr = DomUtil.hasXml(entry.getKeyAttr());
    final boolean hasKeyElement = DomUtil.hasXml(entry.getKey());
    final boolean hasKeyRef = DomUtil.hasXml(entry.getKeyRef());
    if (!hasKeyAttr && !hasKeyElement && !hasKeyRef) {
      holder.createProblem(entry, SpringBundle.message("model.inspection.injection.value.entry.key")).highlightWholeElement();
    } else if (hasKeyAttr && hasKeyElement || hasKeyAttr && hasKeyRef || hasKeyElement && hasKeyRef) {
      final String message = SpringBundle.message("spring.bean.property.value.inconsistency.key");
      if (hasKeyAttr) {
        reportAttribute(entry.getKeyAttr(), holder, message);
      }
      if (hasKeyElement) {
        reportAttribute(entry.getKey(), holder, message);
      }
      if (hasKeyRef) {
        reportAttribute(entry.getKeyRef(), holder, message);
      }
    }
    checkValueHolder(entry, holder, ENTRY);
  }

  private static void checkRef(final SpringRef ref, final DomElementAnnotationHolder holder) {
    if (!DomUtil.hasXml(ref)) return;

    final boolean hasBean = ref.getBean().getXmlAttribute() != null;
    final boolean hasLocal = ref.getLocal().getXmlAttribute() != null;
    final boolean hasParent = ref.getParentAttr().getXmlAttribute() != null;
    if (!hasBean && !hasLocal && !hasParent) {
      holder.createProblem(ref, HighlightSeverity.ERROR,
                           SpringBundle.message("spring.bean.ref.attributes.must.specify"),
                           new AddRefFix(ref.getBean()),
                           new AddRefFix(ref.getLocal()),
                           new AddRefFix(ref.getParentAttr())).highlightWholeElement();

    } else if (hasBean && hasLocal || hasBean && hasParent || hasLocal && hasParent) {
      final String message = SpringBundle.message("spring.bean.ref.attributes.inconsistency");
      reportAttribute(ref.getBean(), holder, message);
      reportAttribute(ref.getLocal(), holder, message);
      reportAttribute(ref.getParentAttr(), holder, message);
    }
  }

  private static void checkIdref(final Idref ref, final DomElementAnnotationHolder holder) {
    if (!DomUtil.hasXml(ref)) return;

    final boolean hasBean = ref.getBean().getXmlAttribute() != null;
    final boolean hasLocal = ref.getLocal().getXmlAttribute() != null;
    if (!hasBean && !hasLocal) {
      holder.createProblem(ref, HighlightSeverity.ERROR,
                           SpringBundle.message("spring.bean.idref.attributes.must.specify"),
                           new AddRefFix(ref.getBean()),
                           new AddRefFix(ref.getLocal())).highlightWholeElement();

    } else if (hasBean && hasLocal) {
      final String message = SpringBundle.message("spring.bean.idref.attributes.inconsistency");
      reportAttribute(ref.getBean(), holder, message);
      reportAttribute(ref.getLocal(), holder, message);
    }
  }

  @NotNull
  private static Set<DomElement> getValues(final SpringElementsHolder elementsHolder) {
    Set<DomElement> values = new THashSet<DomElement>(DomUtil.getDefinedChildrenOfType(elementsHolder, DomSpringBean.class));
    addValue(elementsHolder.getIdref(), values);
    addValue(elementsHolder.getList(), values);
    addValue(elementsHolder.getMap(), values);
    addValue(elementsHolder.getNull(), values);
    addValue(elementsHolder.getProps(), values);
    addValue(elementsHolder.getRef(), values);
    addValue(elementsHolder.getSet(), values);
    addValue(elementsHolder.getValue(), values);
    return values;
  }

  private static void addValue(DomElement valueElement, @NotNull Set<DomElement> values) {
    if (DomUtil.hasXml(valueElement)) {
      values.add(valueElement);
    }
  }

  private static void reportSubtags(@NotNull final Set<DomElement> values, final DomElementAnnotationHolder holder, final String message) {
    for (DomElement value: values) {
      holder.createProblem(value, HighlightSeverity.ERROR, message, new RemoveDomElementQuickFix(value)).highlightWholeElement();
    }
  }

  private static void reportAttribute(final DomElement element, final DomElementAnnotationHolder holder, final String message) {
    if (DomUtil.hasXml(element)) {
      holder.createProblem(element, HighlightSeverity.ERROR, message, new RemoveDomElementQuickFix(element)).highlightWholeElement();
    }
  }

  private static void reportNoValue(final SpringValueHolder injection, final DomElementAnnotationHolder holder, String elementName) {
    final List<? extends PsiType> types = injection.getRequiredTypes();
    final ArrayList<LocalQuickFix> quickFixes = new ArrayList<LocalQuickFix>();
    quickFixes.add(new AddDomElementQuickFix<DomElement>(injection.getValueAttr()));

    for (PsiType type : types) {
      if (type instanceof PsiClassType) {
        final PsiClass psiClass = ((PsiClassType)type).resolve();
        quickFixes.add(0, new AddRefFix(injection.getRefAttr(), psiClass));
        if (psiClass != null) {
          final Project project = psiClass.getProject();
          final PsiManager psiManager = PsiManager.getInstance(project);
          final GlobalSearchScope scope = ProjectScope.getAllScope(project);
          final PsiClass listClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(List.class.getName(), scope);
          if (listClass != null && InheritanceUtil.isInheritorOrSelf(psiClass, listClass, true)) {
            quickFixes.add(0, new AddListFix(injection.getList()));
          }
          else {
            final PsiClass mapClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Map.class.getName(), scope);
            if (mapClass != null && InheritanceUtil.isInheritorOrSelf(psiClass, mapClass, true)) {
              quickFixes.add(0, new AddMapFix(injection.getMap()));
            }
            else {
              final PsiClass setClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(Set.class.getName(), scope);
              if (setClass != null && InheritanceUtil.isInheritorOrSelf(psiClass, setClass, true)) {
                quickFixes.add(0, new AddListFix(injection.getSet()));
              }
            }
          }
        }
      }
      else if (type instanceof PsiArrayType) {
        quickFixes.add(0, new AddListFix(injection.getSet()));
        quickFixes.add(0, new AddListFix(injection.getList()));
      }

    }

    holder.createProblem(injection, HighlightSeverity.ERROR, SpringBundle.message("model.inspection.injection.value.message", elementName),
                         quickFixes.toArray(new LocalQuickFix[quickFixes.size()])).highlightWholeElement();
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("model.inspection.injection.value.consistency");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "SpringInjectionValueConsistencyInspection";
  }

  private static class AddRefFix extends AddDomElementQuickFix<GenericDomValue> {
    @Nullable private final PsiClass myPsiClass;

    public AddRefFix(@NotNull GenericDomValue ref) {
      this(ref, null);
    }

    public AddRefFix(@NotNull GenericDomValue ref, @Nullable PsiClass psiClass) {
      super(ref);
      myPsiClass = psiClass;
    }

    @NotNull
    public String getName() {
      return SpringBundle.message("model.inspection.injection.value.add.ref");
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      super.applyFix(project, descriptor);
      if (myPsiClass != null) {
        final XmlElement element = myElement.getXmlElement();
        assert element != null;
        final XmlFile xmlFile = (XmlFile)element.getContainingFile();
        final SpringModel model = SpringManager.getInstance(project).getSpringModelByFile(xmlFile);
        if (model != null) {
          final List<SpringBaseBeanPointer> list = model.findBeansByEffectivePsiClassWithInheritance(myPsiClass);
          if (list.size() == 1) {
            myElement.setStringValue(SpringUtils.getReferencedName(list.get(0), model.getAllCommonBeans(true)));
          }
        }
      }
    }
  }

  private static class AddListFix extends AddDomElementQuickFix<ListOrSet> {
    public AddListFix(@NotNull ListOrSet listOrSet) {
      super(listOrSet);
    }

    @NotNull
    public String getName() {
      return SpringBundle.message("model.inspection.injection.value.add.list");
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final SpringValue value = myElement.addValue();
      value.setStringValue("x");
      value.setStringValue("");
    }
  }

  private static class AddMapFix extends AddDomElementQuickFix<SpringMap> {
    public AddMapFix(SpringMap map) {
      super(map);
    }

    @NotNull
    public String getName() {
      return SpringBundle.message("model.inspection.injection.value.add.map");
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      myElement.addEntry();
    }
  }
}
