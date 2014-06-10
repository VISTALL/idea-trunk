package com.intellij.seam;

import com.intellij.psi.xml.XmlTag;
import com.intellij.seam.constants.SeamNamespaceConstants;
import com.intellij.util.NotNullFunction;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public abstract class BasicPagesDomFileDescription<T> extends DomFileDescription<T> {
  private static final List<String> PAGES_NAMESPACES =
     Arrays.asList(SeamNamespaceConstants.PAGES_NAMESPACE, SeamNamespaceConstants.PAGES_DTD_1_1, SeamNamespaceConstants.PAGES_DTD_1_2,SeamNamespaceConstants.PAGES_DTD_2_0);

  public BasicPagesDomFileDescription(final Class<T> rootElementClass, @NonNls final String rootTagName, @NonNls final String... allPossibleRootTagNamespaces) {
    super(rootElementClass, rootTagName, allPossibleRootTagNamespaces);
  }

  protected void initializeFileDescription() {
    registerNamespacePolicy(SeamNamespaceConstants.PAGES_NAMESPACE_KEY, new NotNullFunction<XmlTag, List<String>>() {
      @NotNull
      public List<String> fun(final XmlTag tag) {
        return PAGES_NAMESPACES;
      }
    });
  }
}

