package org.jetbrains.android.dom.converters;

import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.android.dom.wrappers.ValueResourceElementWrapper;
import org.jetbrains.android.dom.resources.ResourceValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yole
 */
public class ValueResourceReference extends BaseResourceReference {
  private final List<XmlAttributeValue> myTargets;

  public ValueResourceReference(GenericDomValue<ResourceValue> value, @Nullable Collection<XmlAttributeValue> targets) {
    super(value);
    myTargets = new ArrayList<XmlAttributeValue>(targets);
  }

  @NotNull
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    if (myTargets == null) return ResolveResult.EMPTY_ARRAY;
    List<ResolveResult> result = new ArrayList<ResolveResult>();
    for (XmlAttributeValue target : myTargets) {
      PsiElement e = target instanceof NavigationItem ? new ValueResourceElementWrapper(target) : target;
      result.add(new PsiElementResolveResult(e));
    }
    return result.toArray(new ResolveResult[result.size()]);
  }
}
