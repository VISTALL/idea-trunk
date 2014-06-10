/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.grails.perspectives.create;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassNode;
import static org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo.Relation;
import org.jetbrains.plugins.grails.util.DomainClassUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef.members.GrMethodImpl;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentLabel;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.util.GroovyUtils;

import java.util.*;


/**
 * User: Dmitry.Krasilschikov
 * Date: 14.08.2007
 */
public class RelationsCreationsProvider {
  private static final Logger LOG = Logger.getInstance("org.jetbrains.plugins.grails.perspectives.create.RelationsCreationsProvider");

  private final String newNodeTypeText;
  private final GrTypeDefinition typeDefinition;
  private final Project myProject;

  @NotNull
  private final String enteredName;
  private final GroovyPsiElementFactory factory;

  public RelationsCreationsProvider(String newNodeTypeText, GrTypeDefinition typeDefinition, Project project, String enteredName) {
    this.newNodeTypeText = newNodeTypeText;
    this.typeDefinition = typeDefinition;
    myProject = project;
    this.enteredName = enteredName;

    factory = GroovyPsiElementFactory.getInstance(myProject);
  }

  @NotNull
  public String getEnteredName() {
    return enteredName;
  }

  public void createHasManyRelation() {
    GrField hasManyField = (GrField) typeDefinition.findFieldByName(DomainClassRelationsInfo.HAS_MANY_NAME, false);

    if (hasManyField == null) {
      addNewRelationNode(Relation.HAS_MANY, null, "[" + getEnteredName() + ":" + newNodeTypeText + "]");
    } else {
      assert hasManyField.getInitializerGroovy() instanceof GrListOrMap;

      GrExpression initializerGroovy = hasManyField.getInitializerGroovy();
      assert initializerGroovy instanceof GrListOrMap;

      GrListOrMap belongsToList = (GrListOrMap) initializerGroovy;
      GrNamedArgument[] namedArguments = belongsToList.getNamedArguments();
      StringBuffer newInitializerText = new StringBuffer();

      newInitializerText.append("[");
      for (int i = 0; i < namedArguments.length; i++) {
        GrNamedArgument namedArgument = namedArguments[i];
        GrExpression initializer = namedArgument.getExpression();
        GrArgumentLabel argumentLabel = namedArgument.getLabel();

        assert argumentLabel != null;
        newInitializerText.append(argumentLabel.getText());
        newInitializerText.append(": ");
        newInitializerText.append(initializer.getText());

        if (i != namedArguments.length - 1) {
          newInitializerText.append(", ");
        }
      }
      newInitializerText.append(", ");
      newInitializerText.append(getEnteredName());
      newInitializerText.append(": ");
      newInitializerText.append(newNodeTypeText);
      newInitializerText.append("]");

      addNewRelationNode(Relation.HAS_MANY, hasManyField, newInitializerText.toString());
    }
  }

  public void createBelongsToRelation() {
    GrField belongsToField = (GrField) typeDefinition.findFieldByName(DomainClassRelationsInfo.BELONGS_TO_NAME, false);

    // there is no belongsTo variable
    if (belongsToField == null) {
      addNewRelationNode(Relation.BELONGS_TO, null, "[" + newNodeTypeText + "]");
    } else {

      if (!(belongsToField.getInitializerGroovy() instanceof GrListOrMap)) {
        // static belongsTo = Author
        GrExpression initializerGroovy = belongsToField.getInitializerGroovy();
        assert initializerGroovy != null;
        String newInitializerText = "[" + initializerGroovy.getText() + ", " + newNodeTypeText + "]";

        addNewRelationNode(Relation.BELONGS_TO, belongsToField, newInitializerText);
      } else {
        GrExpression initializerGroovy = belongsToField.getInitializerGroovy();
        assert initializerGroovy instanceof GrListOrMap;

        GrListOrMap belongsToList = (GrListOrMap) initializerGroovy;
        GrExpression[] belongsToListInitializers = belongsToList.getInitializers();

        String newInitializerText = "[";
        for (int i = 0; i < belongsToListInitializers.length; i++) {
          GrExpression initializer = belongsToListInitializers[i];

          newInitializerText += initializer.getText();
          if (i != belongsToListInitializers.length - 1) {
            newInitializerText += ", ";
          }
        }
        newInitializerText += ", " + newNodeTypeText;
        newInitializerText += "]";
        addNewRelationNode(Relation.BELONGS_TO, belongsToField, newInitializerText);
      }
    }
  }

  private void addNewRelationNode(final Relation relationType, final GrField oldRelationField, final String newInitializerText) {
    //    adding new "static belongsTo = [RefExpr1, RefExpr2]" and "RefExpr1 varName" to fields
    if (oldRelationField != null) {
      assert typeDefinition.getBody() == oldRelationField.getParent().getParent();
    }
    final Runnable runWritePsi = new Runnable() {
      public void run() {
        final GrVariableDeclaration newRelationField;
        GrVariableDeclaration treeRelationField = null;
        GrVariableDeclaration treeBelongsToField = null;

        if (Relation.BELONGS_TO == relationType) {
          newRelationField = createRelationFieldDefinition(DomainClassRelationsInfo.BELONGS_TO_NAME, newInitializerText);
          treeBelongsToField = addRelationFieldToPsi(oldRelationField, newRelationField);

          //adding simple var declaration
          GrVariableDeclaration newSimpleVarDeclaration = factory.createSimpleVariableDeclaration(getEnteredName(), newNodeTypeText);
          treeRelationField = addSimpleVarDeclaration(newSimpleVarDeclaration, newRelationField);

        } else if (Relation.HAS_MANY == relationType) {
          newRelationField = createRelationFieldDefinition(DomainClassRelationsInfo.HAS_MANY_NAME, newInitializerText);
          treeRelationField = addRelationFieldToPsi(oldRelationField, newRelationField);

        } else if (Relation.STRONG == relationType) {
          final GrVariableDeclaration newStrongRelation = factory.createSimpleVariableDeclaration(enteredName, newNodeTypeText);
          treeRelationField = addSimpleVarDeclaration(newStrongRelation, getAppropriateAnchor());
        }

        if (treeRelationField != null) {
          PsiUtil.shortenReferences(treeRelationField);
        }
        if (treeBelongsToField != null) {
          PsiUtil.shortenReferences(treeBelongsToField);
        }
      }
    };

    CommandProcessor.getInstance().executeCommand(
        myProject,
        new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(runWritePsi);
          }
        }, "foo", null);
  }

  private GrVariableDeclaration addSimpleVarDeclaration(GrVariableDeclaration simpleVarDef, PsiElement anchor) {
    try {
      return typeDefinition.addMemberDeclaration(simpleVarDef, anchor);
    } catch (IncorrectOperationException e) {
      LOG.error(e);
      return null;
    }
  }

  private GrVariableDeclaration addRelationFieldToPsi(final GrField oldRelationNode, final GrVariableDeclaration newBelongsToVarDef) {
    if (oldRelationNode != null) {
      typeDefinition.getBody().removeVariable(oldRelationNode);
    }

    PsiElement anchor = getAppropriateAnchor();

    try {
      return typeDefinition.addMemberDeclaration(newBelongsToVarDef, anchor);
    } catch (IncorrectOperationException e) {
      LOG.error(e);
      return null;
    }
  }

  private PsiElement getAppropriateAnchor() {
    PsiElement lastBodyChild;
    GrField[] fields = typeDefinition.getFields();
    if (fields.length > 0) {
      PsiElement field = fields[fields.length - 1];
      assert field != null;
      lastBodyChild = field.getParent();
    } else {
      PsiMethod[] methods = typeDefinition.getMethods();
      lastBodyChild = typeDefinition.getBody().getLastChild();

      for (PsiMethod method : methods) {
        if (method instanceof GrMethodImpl){
          lastBodyChild =  method;
          break;
        }
      }
    }
    return lastBodyChild;
  }

  private GrVariableDeclaration createRelationFieldDefinition(String name, String initializerText) {
    GrExpression newInitializer = factory.createExpressionFromText(initializerText);

    return factory.createFieldDeclaration(new String[]{PsiModifier.STATIC}, name, newInitializer, null);
  }

  public boolean canCreateBelongsToRelation() {
    final GrField[] fields = typeDefinition.getFields();


    for (GrField field : fields) {
      PsiType fieldType = field.getType();
      if (fieldType instanceof PsiClassType && newNodeTypeText.equals(((PsiClassType) fieldType).getClassName()))
        return false;

      if (checkForExistingHasManyRelation(field)) return false;
    }

    GrVariable belongsTo = (GrVariable) typeDefinition.findFieldByName(DomainClassRelationsInfo.BELONGS_TO_NAME, false);
    if (belongsTo == null) return true;

    GrExpression list = belongsTo.getInitializerGroovy();

    if (list instanceof GrListOrMap) {
      GrListOrMap initsList = (GrListOrMap) list;
      GrExpression[] initializers = initsList.getInitializers();

      for (GrExpression expression : initializers) {
        if (newNodeTypeText.equals(expression.getText())) return false;
      }
    } else if (list instanceof GrReferenceExpression) {
      GrReferenceExpression initializer = (GrReferenceExpression) list;

      if (newNodeTypeText.equals(initializer.getName())) return false;
    } else {
      return false;
    }

    return true;
  }

  private boolean checkForExistingHasManyRelation(GrField field) {
    Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges = new HashMap<DomainClassNode, List<DomainClassRelationsInfo>>();
    if (DomainClassUtils.isHasManyField(field)) {
      DomainClassUtils.buildHasManySourcesToOutEdgesMap(sourcesToOutEdges, field);

      final Collection<List<DomainClassRelationsInfo>> outEdges = sourcesToOutEdges.values();
      final List<DomainClassRelationsInfo> relationsInfoList = GroovyUtils.flatten(outEdges);
      for (DomainClassRelationsInfo domainClassRelationsInfo : relationsInfoList) {
        if (newNodeTypeText.equals(domainClassRelationsInfo.getTarget().getTypeDefinition().getName())) return true;
      }
    }
    return false;
  }

  public boolean canCreateHasManyRelation() {
    GrField hasManyField = (GrField) typeDefinition.findFieldByName(DomainClassRelationsInfo.HAS_MANY_NAME, false);
    if (hasManyField == null) return true;

    GrExpression map = hasManyField.getInitializerGroovy();
    assert map instanceof GrListOrMap;
    GrListOrMap namesMap = (GrListOrMap) map;
    GrNamedArgument[] arguments = namesMap.getNamedArguments();

    GrArgumentLabel argumentLabel;
    GrExpression type;
    for (GrNamedArgument argument : arguments) {
      argumentLabel = argument.getLabel();
      type = argument.getExpression();

      assert argumentLabel != null;
      if (!(type instanceof GrReferenceExpression)) return false;

      if (newNodeTypeText.equals(((GrReferenceExpression) type).getName())) return false;
      if (enteredName.equals(argumentLabel.getText())) return false;
    }

    return true;
  }

  public boolean canCreateStrongRelation() {
    final GrField[] fields = typeDefinition.getFields();
    for (GrField field : fields) {
      if (enteredName.equals(field.getName())) return false;

      if (checkForExistingHasManyRelation(field)) return false;
    }

    return true;
  }

  public void createStrongRelation() {
    addNewRelationNode(Relation.STRONG, null, null);
  }
}