package org.jetbrains.plugins.groovy.grails.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.*;
import com.intellij.util.containers.hash.HashSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo;
import org.jetbrains.plugins.grails.util.DomainClassUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentLabel;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;

import java.util.Map;
import java.util.Set;

/**
 * User: Dmitry.Krasilschikov
 * Date: 16.08.2007
 */
public class DomainClassAnnotator implements Annotator {
  public void annotate(PsiElement psiElement, AnnotationHolder holder) {
    if (!(psiElement instanceof GroovyFile)) return;

    GroovyFile groovyFile = (GroovyFile)psiElement;
    if (!DomainClassUtils.isDomainClassFile(groovyFile.getVirtualFile(), groovyFile.getProject())) {
      return;
    }

    GrTypeDefinition[] grTypeDefinitions = groovyFile.getTypeDefinitions();

    for (GrTypeDefinition grTypeDefinition : grTypeDefinitions) {
      checkBelongsToAndHasManyRelations(grTypeDefinition, holder);
    }
  }

  private static void checkBelongsToAndHasManyRelations(PsiClass domainClass, AnnotationHolder holder) {
    final Map<String, PsiType> fields = DomainClassUtils.getDomainFields(domainClass);

    final PsiField belongs = domainClass.findFieldByName(DomainClassRelationsInfo.BELONGS_TO_NAME, false);
    GrListOrMap lom = getListOrMap(belongs);
    if (lom != null) {
      if (lom.isMap()) {
        for (GrNamedArgument argument : lom.getNamedArguments()) {
          checkNamedArgument(argument, fields, holder, true);
        }
      }
      processDuplicates(holder, lom);
    }

    final PsiField hasMany = domainClass.findFieldByName(DomainClassRelationsInfo.HAS_MANY_NAME, false);
    lom = getListOrMap(hasMany);
    if (lom != null) {
      if (lom.isMap()) {
        for (GrNamedArgument argument : lom.getNamedArguments()) {
          checkNamedArgument(argument, fields, holder, false);
        }
      }
      else {
        holder.createWarningAnnotation(lom, GrailsBundle.message("hasmany.must.contain.map"));
      }
      processDuplicates(holder, lom);
    }
  }

  private static void checkNamedArgument(GrNamedArgument argument,
                                         Map<String, PsiType> fields,
                                         AnnotationHolder holder,
                                         boolean checkBelongsTo) {
    final GrArgumentLabel label = argument.getLabel();
    if (label != null) {
      final GrExpression expression = argument.getExpression();
      PsiClass labelClass = null;
      if (expression instanceof GrReferenceExpression) {
        final PsiElement element = ((GrReferenceExpression)expression).resolve();
        if (element instanceof PsiClass && DomainClassUtils.isDomainClass((PsiClass)element)) {
          labelClass = (PsiClass)element;
        }
      }
      if (labelClass == null) {
        holder.createWarningAnnotation(expression != null ? expression : argument, GrailsBundle.message("must.be.domain.class.name"));
        return;
      }

      final String name = label.getName();
      final PsiType type = fields.get(name);
      if (type instanceof PsiClassType) {
        PsiClass fieldClass = ((PsiClassType)type).resolve();
        PsiManager manager = PsiManager.getInstance(argument.getProject());
        if (checkBelongsTo &&
            fieldClass != null &&
            !(manager.areElementsEquivalent(fieldClass, labelClass) || labelClass.isInheritor(fieldClass, true))) {
          holder.createWarningAnnotation(argument, GrailsBundle.message("property.is.abmbigous", name, fieldClass.getQualifiedName()));
        }
      }
    }
  }


  @Nullable
  private static GrListOrMap getListOrMap(@Nullable PsiField field) {
    if (field instanceof GrField) {
      final GrExpression initializer = ((GrField)field).getInitializerGroovy();
      if (initializer instanceof GrListOrMap) {
        return (GrListOrMap)initializer;
      }
    }
    return null;
  }

  private static void processDuplicates(AnnotationHolder holder, GrListOrMap lom) {
    Set<String> names = new HashSet<String>();
    Set<String> classNames = new HashSet<String>();
    if (lom.isMap()) {
      for (GrNamedArgument argument : lom.getNamedArguments()) {
        final GrArgumentLabel label = argument.getLabel();
        if (label != null) {
          final String name = label.getName();
          if (names.contains(name)) {
            holder.createWarningAnnotation(label, GrailsBundle.message("duplicate.property.name"));
          }
          else {
            names.add(name);
          }
        }

        final GrExpression expr = argument.getExpression();
        if (expr instanceof GrReferenceExpression) {
          final PsiElement element = ((GrReferenceExpression)expr).resolve();
          if (element instanceof PsiClass) {
            final String className = ((PsiClass)element).getQualifiedName();
            if (classNames.contains(className)) {
              holder.createWarningAnnotation(expr, GrailsBundle.message("duplicate.type", "map"));
            }
            else {
              classNames.add(className);
            }
          }
        }
      }
    }
    else {
      for (GrExpression expr : lom.getInitializers()) {
        PsiElement element = null;
        if (expr instanceof GrReferenceExpression) {
          element = ((GrReferenceExpression)expr).resolve();
          if (element instanceof PsiClass) {
            final String className = ((PsiClass)element).getQualifiedName();
            if (classNames.contains(className)) {
              holder.createWarningAnnotation(expr, GrailsBundle.message("duplicate.type", "list"));
            }
            else {
              classNames.add(className);
            }
          }
        }
        if (!(element instanceof PsiClass && DomainClassUtils.isDomainClass((PsiClass)element))) {
          holder.createWarningAnnotation(expr, GrailsBundle.message("must.be.domain.class.name"));
        }
      }
    }
  }
}
