package org.jetbrains.plugins.grails.lang.gsp.parsing.composite;

import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.xml.IXmlElementType;

/**
 * @author ilyas
 */
public abstract class GspCompositeElementType extends IXmlElementType implements ICompositeElementType {
  public GspCompositeElementType(String debugName) {
    super(debugName);
  }
}
