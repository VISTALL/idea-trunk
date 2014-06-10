package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.index.FileBasedWSIndex;
import com.advancedtools.webservices.index.WSIndexEntry;
import com.advancedtools.webservices.jaxb.JaxbMappingEngine;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.utils.JavaElementVisitor;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

/**
 * @by Maxim
 */                      
public abstract class BaseWebServicesInspection extends LocalInspectionTool {
  protected boolean myIsOnTheFly;

  protected static LocalQuickFix[] EMPTY = new LocalQuickFix[0];
  private static final HashSet<String> ourAnnotationsForExternallyBoundClasses = new HashSet<String>();
  @NonNls
  static final String WEB_SERVICE_ANNOTATION_NAME = "WebService";
  private static final Key<CachedValue<ExternallyBoundClassContext>> ourClassContextKey = Key.create("webservices.externally.bound.class");

  static {
    ourAnnotationsForExternallyBoundClasses.addAll(JWSDPWSEngine.wsClassesSet);
    ourAnnotationsForExternallyBoundClasses.addAll(JaxbMappingEngine.mappedClassesSet);
  }

  @NotNull
  public String getGroupDisplayName() {
    return WSBundle.message("webservices.inspections.group.name");
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  private static boolean isNotAcceptableMember(PsiMember c) {
    if (c instanceof PsiMethod && ((PsiMethod)c).isConstructor()) return true;
    final PsiClass psiClass = c instanceof PsiClass ? (PsiClass) c:c.getContainingClass();
    if (psiClass instanceof PsiAnonymousClass ||
        psiClass instanceof PsiTypeParameter
       ) {
      return true;
    }
    final FileType fileType = c.getContainingFile().getFileType();
    return fileType == StdFileTypes.JSP || fileType == StdFileTypes.JSPX;
  }

  protected static ExternallyBoundClassContext getClassContext(@NotNull final PsiClass containingClass) {
    CachedValue<ExternallyBoundClassContext> value = containingClass.getUserData(ourClassContextKey);

    if (value == null) {
      value = containingClass.getManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<ExternallyBoundClassContext>() {
        public Result<ExternallyBoundClassContext> compute() {
          return new Result<ExternallyBoundClassContext>(
            new ExternallyBoundClassContext(containingClass),
            new ModificationTracker() {
              final PsiModificationTracker tracker = containingClass.getManager().getModificationTracker();

              public long getModificationCount() {
                return tracker.getOutOfCodeBlockModificationCount();
              }
            }
          );
        }
      }, false);
      containingClass.putUserData(ourClassContextKey, value);
    }
    return value.getValue();
  }

  static class ExternallyBoundClassContext {
    final FileBasedWSIndex index;
    final boolean externallyBound;
    final boolean containingClassIsExternallyBound;
    final boolean isInterface;
    final PsiAnnotation annotation;
    private final String key;

    ExternallyBoundClassContext(PsiClass c) {
      index = FileBasedWSIndex.getInstance();
      key = c.getName();
      containingClassIsExternallyBound = (annotation = AnnotationUtil.findAnnotation(c, ourAnnotationsForExternallyBoundClasses)) != null;

      if (index.getWsEntries(c).length == 0 && !containingClassIsExternallyBound) {
        isInterface = externallyBound = false;
      } else {
        externallyBound = true;
        isInterface = c.isInterface();
      }
    }

    final boolean isExternallyBound() {
      return externallyBound && !isInterface;
    }

    public long getModificationStamp() {
      return index.getModificationStamp();
    }

    public WSIndexEntry[] getEntries(Module module) {
      if (module == null) return WSIndexEntry.EMPTY;
      List<WSIndexEntry> entries = FileBasedIndex.getInstance().getValues(FileBasedWSIndex.WS_INDEX, key, GlobalSearchScope.moduleScope(module));
      for (WSIndexEntry entry : entries) {
        if (! entry.isResolved(module.getProject())) entry.resolve(module.getProject());
      }
      return entries.isEmpty() ? WSIndexEntry.EMPTY : entries.toArray(new WSIndexEntry[entries.size()]);
    }
  }

  @NotNull
  public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder problemsHolder, boolean onTheFly) {
    return new JavaElementVisitor() {
      @Override public void visitMethod(PsiMethod psiMethod) {
        if (isNotAcceptableMember(psiMethod)) return;
        checkMember(problemsHolder, psiMethod);
      }

      @Override public void visitField(PsiField psiField) {
        if (isNotAcceptableMember(psiField)) return;
        checkMember(problemsHolder, psiField);
      }

      @Override public void visitClass(PsiClass c) {
        if (isNotAcceptableMember(c)) return;
        doCheckClass(c, problemsHolder);
      }
    };
  }

  protected abstract void checkMember(ProblemsHolder problemsHolder, PsiMember psiMember);
  protected abstract void doCheckClass(PsiClass c, ProblemsHolder problemsHolder);
}
