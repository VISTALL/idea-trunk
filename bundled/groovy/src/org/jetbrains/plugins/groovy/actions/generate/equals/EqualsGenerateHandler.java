package org.jetbrains.plugins.groovy.actions.generate.equals;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.generation.*;
import com.intellij.codeInsight.generation.ui.GenerateEqualsWizard;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.actions.generate.GroovyCodeInsightBundle;
import org.jetbrains.plugins.groovy.actions.generate.constructors.GroovyGenerationInfo;

import java.util.Collection;
import java.util.List;

/**
 * User: Dmitry.Krasilschikov
 * Date: 28.05.2008
 */
public class EqualsGenerateHandler extends GenerateMembersHandlerBase {

  private static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.groovy.actions.generate.equals.EqualsGenerateHandler");
  private PsiField[] myEqualsFields = null;
  private PsiField[] myHashCodeFields = null;
  private PsiField[] myNonNullFields = null;
  private static final PsiElementClassMember[] DUMMY_RESULT = new PsiElementClassMember[1];

  public EqualsGenerateHandler() {
    super("");
  }


  @Nullable
  protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project) {
    myEqualsFields = null;
    myHashCodeFields = null;
    myNonNullFields = PsiField.EMPTY_ARRAY;

    GlobalSearchScope scope = aClass.getResolveScope();
    final PsiMethod equalsMethod = GrGenerateEqualsHelper.findMethod(aClass, GrGenerateEqualsHelper.getEqualsSignature(project, scope));
    final PsiMethod hashCodeMethod = GrGenerateEqualsHelper.findMethod(aClass, GrGenerateEqualsHelper.getHashCodeSignature());

    boolean needEquals = equalsMethod == null;
    boolean needHashCode = hashCodeMethod == null;
    if (!needEquals && !needHashCode) {
      String text = aClass instanceof PsiAnonymousClass
          ? GroovyCodeInsightBundle.message("generate.equals.and.hashcode.already.defined.warning.anonymous")
          : GroovyCodeInsightBundle.message("generate.equals.and.hashcode.already.defined.warning", aClass.getQualifiedName());

      if (Messages.showYesNoDialog(project, text,
          GroovyCodeInsightBundle.message("generate.equals.and.hashcode.already.defined.title"),
          Messages.getQuestionIcon()) == DialogWrapper.OK_EXIT_CODE) {
        if (!ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
          public Boolean compute() {
            try {
              equalsMethod.delete();
              hashCodeMethod.delete();
              return Boolean.TRUE;
            }
            catch (IncorrectOperationException e) {
              LOG.error(e);
              return Boolean.FALSE;
            }
          }
        }).booleanValue()) {
          return null;
        } else {
          needEquals = needHashCode = true;
        }
      } else {
        return null;
      }
    }

    GenerateEqualsWizard wizard = new GenerateEqualsWizard(project, aClass, needEquals, needHashCode);
    wizard.show();
    if (!wizard.isOK()) return null;
    myEqualsFields = wizard.getEqualsFields();
    myHashCodeFields = wizard.getHashCodeFields();
    myNonNullFields = wizard.getNonNullFields();
    return DUMMY_RESULT;
  }

  @NotNull
  protected List<? extends GenerationInfo> generateMemberPrototypes(PsiClass aClass, ClassMember[] originalMembers) throws IncorrectOperationException {
    Project project = aClass.getProject();
    final boolean useInstanceofToCheckParameterType = CodeInsightSettings.getInstance().USE_INSTANCEOF_ON_EQUALS_PARAMETER;

    GrGenerateEqualsHelper helper = new GrGenerateEqualsHelper(project, aClass, myEqualsFields, myHashCodeFields, myNonNullFields, useInstanceofToCheckParameterType);
    Collection<PsiMethod> methods = helper.generateMembers();
    return ContainerUtil.map2List(methods, new Function<PsiMethod, PsiGenerationInfo<PsiMethod>>() {
      public PsiGenerationInfo<PsiMethod> fun(final PsiMethod s) {
        return new GroovyGenerationInfo<PsiMethod>(s);
      }
    });
  }

  protected ClassMember[] getAllOriginalMembers(PsiClass aClass) {
    return ClassMember.EMPTY_ARRAY;
  }

  protected GenerationInfo[] generateMemberPrototypes(PsiClass aClass, ClassMember originalMember) throws IncorrectOperationException {
    return GenerationInfo.EMPTY_ARRAY;
  }

  protected void cleanup() {
    super.cleanup();

    myEqualsFields = null;
    myHashCodeFields = null;
    myNonNullFields = null;
  }

  public boolean startInWriteAction() {
      return true;
    } 
}
