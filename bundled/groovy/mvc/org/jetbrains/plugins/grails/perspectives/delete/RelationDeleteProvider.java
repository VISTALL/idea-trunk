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

package org.jetbrains.plugins.grails.perspectives.delete;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.graph.builder.DeleteProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassNode;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentLabel;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dmitry.Krasilschikov
 * Date: 29.08.2007
 */
public class RelationDeleteProvider extends DeleteProvider<DomainClassNode, DomainClassRelationsInfo> {
  private final Project myProject;

  public RelationDeleteProvider(Project project) {
    myProject = project;
  }

  public boolean canDeleteNode(@NotNull DomainClassNode node) {
    return false;
  }

  public boolean canDeleteEdge(@NotNull DomainClassRelationsInfo edge) {
    return true;
  }

  public boolean deleteNode(@NotNull DomainClassNode node) {
    return false;
  }

  public boolean deleteEdge(@NotNull DomainClassRelationsInfo edge) {
    final DomainClassNode targetCLassNode = edge.getTarget();
    final DomainClassNode sourceClassNode = edge.getSource();
    final String varName = edge.getVarName();

    final DomainClassRelationsInfo.Relation relationType = edge.getRelation();
    final Runnable runnable;

    if (DomainClassRelationsInfo.Relation.BELONGS_TO == relationType) {
      runnable = deleteBelongsToRelation(targetCLassNode, sourceClassNode);

    } else if (DomainClassRelationsInfo.Relation.HAS_MANY == relationType) {
      runnable = deleteHasManyRelation(varName, sourceClassNode);

    } else if (DomainClassRelationsInfo.Relation.STRONG == relationType) {
      runnable = deleteStrongRelation(varName, sourceClassNode);

    } else {
      runnable = new Runnable() {
        public void run() {
        }
      };
    }

    CommandProcessor.getInstance().executeCommand(
        myProject,
        new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(runnable);
          }
        }, "foo", null);

    return false;
  }

  private Runnable deleteStrongRelation(final String varName, final DomainClassNode sourceClassNode) {
    return new Runnable() {
      public void run() {
        deleteStrongField(sourceClassNode.getTypeDefinition(), varName);
      }
    };
  }

  private Runnable deleteBelongsToRelation(final DomainClassNode targetCLassNode, final DomainClassNode sourceClassNode) {
    return new Runnable() {
      public void run() {
        final PsiClass sourceTypeDefinition = sourceClassNode.getTypeDefinition();
        GrField belongsToField = (GrField) sourceTypeDefinition.findFieldByName(DomainClassRelationsInfo.BELONGS_TO_NAME, false);
        assert belongsToField != null;
        final ASTNode belongsToNode = belongsToField.getNode();
        assert belongsToNode != null;

        GrExpression initializerGroovy = belongsToField.getInitializerGroovy();
        String fieldBelongsToItemType = "";

        if (initializerGroovy instanceof GrListOrMap) {
          GrListOrMap belongsToList = (GrListOrMap) initializerGroovy;
          GrExpression[] refExpressions;

          //before removing
          refExpressions = belongsToList.getInitializers();
          //elements list, reference expressions
          final String name = targetCLassNode.getTypeDefinition().getName();
          assert name != null;

          for (GrExpression initializer : refExpressions) {
            assert initializer instanceof GrReferenceExpression;


            if (name.equals(initializer.getText())) {
              final ASTNode node = initializer.getParent().getNode();
              fieldBelongsToItemType = ((GrReferenceExpression) initializer).getName();

              assert node != null;
              node.removeChild(initializer.getNode());

              break;
            }
          }

          //after removing
          refExpressions = belongsToList.getInitializers();
          if (refExpressions.length == 0) {
            ((GrTypeDefinition) sourceTypeDefinition).getBody().removeVariable(belongsToField);
          } else {
            removesSurplusCommas(belongsToList, GrReferenceExpression.class);
          }

        } else if (initializerGroovy instanceof GrReferenceExpression) {
          //removes belongsTo node
          fieldBelongsToItemType = initializerGroovy.getText();

          ((GrTypeDefinition) sourceTypeDefinition).getBody().removeVariable(belongsToField);
        } else {
          return;
        }

        deleteBelongsToItemField(sourceTypeDefinition, fieldBelongsToItemType);
        PsiUtil.reformatCode(sourceClassNode.getTypeDefinition());
      }
    };
  }

  private void deleteBelongsToItemField(PsiClass sourceTypeDefinition, String fieldBelongsToItemType) {
    if (!(sourceTypeDefinition instanceof GrTypeDefinition)) return; //??
    final GrField[] fields = ((GrTypeDefinition) sourceTypeDefinition).getFields();

    for (GrField field : fields) {
      if (fieldBelongsToItemType.equals(field.getType().getPresentableText())) {
        ((GrTypeDefinition) sourceTypeDefinition).getBody().removeVariable(field);
      }
    }
  }

  private void deleteStrongField(PsiClass sourceTypeDefinition, String strongFieldName) {
    if (!(sourceTypeDefinition instanceof GrTypeDefinition)) return; //??
    final GrField[] fields = ((GrTypeDefinition) sourceTypeDefinition).getFields();

    for (GrField field : fields) {
      if (strongFieldName.equals(field.getName())) {
        ((GrTypeDefinition) sourceTypeDefinition).getBody().removeVariable(field);
      }
    }
  }

  private Runnable deleteHasManyRelation(final String varName, final DomainClassNode sourceClassNode) {
    return new Runnable() {
      public void run() {
        final PsiClass sourceTypeDefinition = sourceClassNode.getTypeDefinition();
        GrField hasManyField = (GrField) sourceTypeDefinition.findFieldByName(DomainClassRelationsInfo.HAS_MANY_NAME, false);
        if (hasManyField == null || !(hasManyField.getInitializerGroovy() instanceof GrListOrMap)) {
          return;
        }

        GrExpression initializerGroovy = hasManyField.getInitializerGroovy();
        if (!(initializerGroovy instanceof GrListOrMap)) {
          return;
        }

        GrListOrMap hasManyList = (GrListOrMap) initializerGroovy;
        GrNamedArgument[] namedArguments;

        //before removing
        namedArguments = hasManyList.getNamedArguments();

        assert varName != null;
        for (GrNamedArgument namedArgument : namedArguments) {
          GrArgumentLabel argumentLabel = namedArgument.getLabel();

          assert argumentLabel != null;

          if (varName.equals(argumentLabel.getName())) {
            final ASTNode node = namedArgument.getParent().getNode();
            if (node == null) {
              return;
            }

            node.removeChild(namedArgument.getNode());
          }
        }

        //after removing
        namedArguments = hasManyList.getNamedArguments();
        if (namedArguments.length == 0) {
          ((GrTypeDefinition) sourceTypeDefinition).getBody().removeVariable(hasManyField);
        } else {
          removesSurplusCommas(hasManyList, GrNamedArgument.class);
        }

        PsiUtil.reformatCode(sourceClassNode.getTypeDefinition());
      }
    };
  }

  private void removesSurplusCommas(GrListOrMap hasManyList, Class listElementClass) {
    //removes ',' if needs
    final List<PsiElement> children = new ArrayList<PsiElement>();

    PsiElement tempChild = hasManyList.getFirstChild();
    while (tempChild != null) {
      if (!(tempChild instanceof PsiWhiteSpace)) {
        children.add(tempChild);
      }
      tempChild = tempChild.getNextSibling();
    }

    int i = 1;
    while (i < children.size() - 1) {
      PsiElement child = children.get(i);
      final ASTNode node = child.getNode();
      assert node != null;
      if (GroovyTokenTypes.mCOMMA.equals(node.getElementType())) {
        final PsiElement prevSibling = children.get(i - 1);
        final PsiElement nextSibling = children.get(i + 1);

        if (!listElementClass.isAssignableFrom(prevSibling.getClass()) || !listElementClass.isAssignableFrom(nextSibling.getClass())) {
          node.getTreeParent().removeChild(node);
          children.remove(i);
          continue;
        }
      }
      i++;
    }
  }
}
