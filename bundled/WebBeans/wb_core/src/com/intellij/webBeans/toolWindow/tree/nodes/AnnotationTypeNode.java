package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedMembersSearch;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.Icons;
import com.intellij.util.Processor;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class AnnotationTypeNode extends PsiMemberSimpleNode {
  protected Module myModule;
  protected PsiClass myAnnoClass;

  public AnnotationTypeNode(@NotNull Module module, @NotNull PsiClass annoClass, SimpleNode parent) {
    super(parent, annoClass);

    myModule = module;
    myAnnoClass = annoClass;

    setPlainText(annoClass.getName());
    setUniformIcon(Icons.ANNOTATION_TYPE_ICON);
  }

  @Override
  protected void doUpdate() {
    super.doUpdate();
    clearColoredText();
    addColoredFragment(myAnnoClass.getName(), myAnnoClass.getQualifiedName(), getPlainAttributes());
  }

  public SimpleNode[] getChildren() {
    Collection<PsiMemberSimpleNode> children =createSortedList();
    
    for (PsiMember psiMember : AnnotatedMembersSearch.search(myAnnoClass, GlobalSearchScope.moduleWithLibrariesScope(myModule)).findAll()) {
      children.add(new AnnotatedMembersNode(myModule, psiMember, this));
    }

    return children.toArray(new SimpleNode[children.size()]);
  }

  @Override
  public Object[] getEqualityObjects() {
    return new Object[]{myAnnoClass};
  }

  protected List<PsiClass> findAnnotatedClasses() {
    return findAnnotatedMembers(PsiClass.class);
  }

  protected List<PsiMethod> findAnnotatedMethods() {
    return findAnnotatedMembers(PsiMethod.class);
  }

  protected <T extends PsiModifierListOwner> List<T> findAnnotatedMembers(Class<T> clazz) {
    final List<T> result = CollectionFactory.arrayList();
    Processor<T> processor = new Processor<T>() {
      public boolean process(final T t) {
        ContainerUtil.addIfNotNull(t, result);
        return true;
      }
    };
    AnnotationModelUtil.findAnnotatedElements(clazz,
                                              myAnnoClass.getQualifiedName(),
                                              PsiManager.getInstance(myModule.getProject()),
                                              GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule),
                                              processor);
    return result;
  }
}
