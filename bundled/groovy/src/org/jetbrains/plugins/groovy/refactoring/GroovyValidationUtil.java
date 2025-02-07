package org.jetbrains.plugins.groovy.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.*;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.clauses.GrForClause;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameter;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.params.GrParameterList;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;

import java.util.ArrayList;

/**
 * @author ilyas
 */
public class GroovyValidationUtil {

  public static boolean validateNewParameterName(GrParameter variable, ArrayList<String> conflicts, @NotNull String varName) {
    GrParameterList list = PsiTreeUtil.getParentOfType(variable, GrParameterList.class);
    GrParametersOwner owner = PsiTreeUtil.getParentOfType(variable, GrParametersOwner.class);
    assert owner != null;
    for (GrParameter parameter : list.getParameters()) {
      validateVariableOccurrencesDownImpl(parameter, conflicts, varName);
    }
    validateVariableOccurrencesDown(owner, list, conflicts, varName);
    PsiElement parent = owner.getParent();
    validateVariableOccurrencesUp(parent, owner, conflicts, varName, parent instanceof GroovyFile);
    return conflicts.size() == 0;
  }


  private static void validateVariableOccurrencesUp(PsiElement parent,
                                                    PsiElement lastParent,
                                                    ArrayList<String> conflicts,
                                                    @NotNull String varName,
                                                    final boolean containerIsFile) {
    if (!containerIsFile && (parent instanceof PsiFile) || parent == null) return;

    PsiElement child = parent.getFirstChild();
    while (child != null && child != lastParent) { // Upper variable declarations
      if (child instanceof GrVariableDeclaration) {
        for (GrVariable variable : ((GrVariableDeclaration)child).getVariables()) {
          if (varName.equals(variable.getName())) {
            addConflict(varName, variable, conflicts);
          }
        }
      }
      child = child.getNextSibling();
    }
    if (parent instanceof GrParametersOwner) { //method or closure parameters
      GrParametersOwner owner = (GrParametersOwner)parent;
      for (GrParameter parameter : owner.getParameters()) {
        if (varName.equals(parameter.getName())) {
          addConflict(varName, parameter, conflicts);
        }
      }
    } else if (parent instanceof GrForStatement) { // For statement binding
      GrForStatement statement = (GrForStatement)parent;
      GrForClause clause = statement.getClause();
      if (clause != null) {
        for (GrVariable variable : clause.getDeclaredVariables()) {
          if (varName.equals(variable.getName())) {
            addConflict(varName, variable, conflicts);
          }
        }
      }
    }
    if (parent instanceof PsiFile) return;
    validateVariableOccurrencesUp(parent.getParent(), parent, conflicts, varName, false);
  }


  private static void validateVariableOccurrencesDown(PsiElement parent,
                                                      PsiElement startChild,
                                                      ArrayList<String> conflicts,
                                                      @NotNull String varName) {
    PsiElement child = parent.getLastChild();
    while (child != null && child != startChild && !(child instanceof GrTypeDefinition)) {
      validateVariableOccurrencesDownImpl(child, conflicts, varName);
      child = child.getPrevSibling();
    }
  }

  private static void validateVariableOccurrencesDownImpl(final PsiElement child, final ArrayList<String> conflicts, final String varName) {
    if (child instanceof PsiNamedElement) {
      PsiNamedElement element = (PsiNamedElement)child;
      if (varName.equals(element.getName())) {
        addConflict(varName, element, conflicts);
      } else {
        for (PsiElement psiElement : child.getChildren()) {
          if (!(child instanceof GrTypeDefinition)) {
            validateVariableOccurrencesDownImpl(psiElement, conflicts, varName);
          }
        }
      }
    }
  }

  private static void addConflict(final String varName, final PsiNamedElement element, final ArrayList<String> conflicts) {
    if (element instanceof GrParameter) {
      conflicts.add(GroovyRefactoringBundle.message("variable.conflicts.with.parameter.0", CommonRefactoringUtil.htmlEmphasize(varName)));
    } else if (element instanceof GrField) {
      conflicts.add(GroovyRefactoringBundle.message("variable.conflicts.with.field.0", CommonRefactoringUtil.htmlEmphasize(varName)));
    } else {
      conflicts.add(GroovyRefactoringBundle.message("variable.conflicts.with.variable.0", CommonRefactoringUtil.htmlEmphasize(varName)));
    }
  }

  public static class ParameterNameSuggester {
    private final String myName;
    private final GrParameter myParameter;

    public ParameterNameSuggester(String name, GrParameter parameter) {
      myName = name;
      myParameter = parameter;
    }

    public String generateName() {
      String name = myName;
      int i = 1;
      ArrayList<String> confl = new ArrayList<String>();
      while (!validateNewParameterName(myParameter, confl, name)) {
        name = myName + i;
        i++;
        confl = new ArrayList<String>();
      }
      return name;
    }
  }


}
