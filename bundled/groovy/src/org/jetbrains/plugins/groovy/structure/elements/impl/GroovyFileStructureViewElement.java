package org.jetbrains.plugins.groovy.structure.elements.impl;

import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.GrTopStatement;
import org.jetbrains.plugins.groovy.structure.elements.GroovyStructureViewElement;
import org.jetbrains.plugins.groovy.structure.itemsPresentations.impl.GroovyFileItemPresentation;

import java.util.ArrayList;
import java.util.List;


/**
 * User: Dmitry.Krasilschikov
 * Date: 30.10.2007
 */
public class GroovyFileStructureViewElement extends GroovyStructureViewElement {
  public GroovyFileStructureViewElement(GroovyFileBase groovyFileBase) {
    super(groovyFileBase);
  }

  public ItemPresentation getPresentation() {
    return new GroovyFileItemPresentation((GroovyFileBase) myElement);
  }

  public TreeElement[] getChildren() {
    List<GroovyStructureViewElement> children = new ArrayList<GroovyStructureViewElement>();

    for (GrTopStatement topStatement : ((GroovyFileBase) myElement).getTopStatements()) {
      if (topStatement instanceof GrTypeDefinition) {
        children.add(new GroovyTypeDefinitionStructureViewElement(((GrTypeDefinition)topStatement)));
      } else if (topStatement instanceof GrMethod) {
        children.add(new GroovyMethodStructureViewElement(((GrMethod) topStatement), false));

      } else if (topStatement instanceof GrVariableDeclaration) {
        for (final GrVariable variable : ((GrVariableDeclaration) topStatement).getVariables()) {
          children.add(new GroovyVariableStructureViewElement(variable));
        }
      }
    }

    return children.toArray(new GroovyStructureViewElement[children.size()]);
  }
}
