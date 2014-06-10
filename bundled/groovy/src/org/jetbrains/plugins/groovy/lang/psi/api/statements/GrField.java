package org.jetbrains.plugins.groovy.lang.psi.api.statements;

import com.intellij.psi.PsiField;
import com.intellij.psi.StubBasedPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocCommentOwner;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrAccessorMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMember;
import org.jetbrains.plugins.groovy.lang.psi.stubs.GrFieldStub;

import java.util.Set;

/**
 * @author ven
 */
public interface GrField extends GrVariable, GrMember, PsiField, GrTopLevelDefintion, StubBasedPsiElement<GrFieldStub>, GrDocCommentOwner {
  public static final GrField[] EMPTY_ARRAY = new GrField[0];

  boolean isProperty();

  @Nullable
  GrAccessorMethod getSetter();

  @NotNull
  GrAccessorMethod[] getGetters();

  @NotNull
  public Set<String>[] getNamedParametersArray();
}
