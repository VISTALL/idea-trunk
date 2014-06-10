package com.intellij.spring.model.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.spring.java.SpringJavaClassInfo;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.DomSpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class SpringRequiredAnnotationInspection  extends BaseJavaLocalInspectionTool {

  @Override
  public ProblemDescriptor[] checkMethod(@NotNull final PsiMethod method, @NotNull final InspectionManager manager,
                                         final boolean isOnTheFly) {

    if (PropertyUtil.isSimplePropertySetter(method)) {
      final SpringJavaClassInfo info = SpringJavaClassInfo.getSpringJavaClassInfo(method.getContainingClass());
      if (info.isMapped()) {
        if (method.getModifierList().findAnnotation(SpringAnnotationsConstants.REQUIRED_ANNOTATION) != null) {
          final String property = PropertyUtil.getPropertyNameBySetter(method);
          final Collection<SpringPropertyDefinition> mappedProperties = info.getMappedProperties(property);
          if (mappedProperties.isEmpty()) {
            final List<DomSpringBeanPointer> list = info.getMappedBeans();
            final List<SpringBean> beans = new ArrayList<SpringBean>(list.size());
            for (DomSpringBeanPointer pointer : list) {
              final DomSpringBean springBean = pointer.getSpringBean();
              if (springBean instanceof SpringBean && !((SpringBean)springBean).isAbstract()) {
                beans.add((SpringBean)springBean);
              }
            }
            if (beans.isEmpty()) {
              return null;
            }

            final LocalQuickFix fix = new LocalQuickFix() {
              @NotNull
              public String getName() {
                return SpringBundle.message("create.missing.mappings", property);
              }

              @NotNull
              public String getFamilyName() {
                return getName();
              }

              public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
                final Set<VirtualFile> files = new HashSet<VirtualFile>();
                for (SpringBean bean : beans) {
                  final PsiFile psiFile = bean.getContainingFile();
                  if (psiFile != null) {
                    files.add(psiFile.getVirtualFile());
                  }
                }
                if (!ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(files).hasReadonlyFiles()) {
                  for (SpringBean bean : beans) {
                    bean.addProperty().getName().setStringValue(property);
                  }
                }
              }
            };
            final PsiIdentifier psiIdentifier = method.getNameIdentifier();
            assert psiIdentifier != null;
            final ProblemDescriptor descriptor = manager
                .createProblemDescriptor(psiIdentifier, SpringBundle.message("required.property.not.mapped", property), fix,
                                         ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            return new ProblemDescriptor[] {descriptor};
          }
        }
      }
    }

    return null;
  }

  @NotNull
  public String getGroupDisplayName() {
    return SpringBundle.message("model.inspection.group.name");
  }

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("required.properties.inspection");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringRequiredAnnotationInspection";
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}
