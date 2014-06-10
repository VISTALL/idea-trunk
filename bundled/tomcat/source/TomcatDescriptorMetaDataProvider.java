package org.jetbrains.idea.tomcat;

import com.intellij.javaee.facet.DescriptorMetaDataProvider;
import com.intellij.javaee.web.facet.WebFacet;
import org.jetbrains.annotations.NotNull;

/**
 * @author nik
 */
public class TomcatDescriptorMetaDataProvider extends DescriptorMetaDataProvider {
  public void registerDescriptors(@NotNull final MetaDataRegistry registry) {
    registry.register(WebFacet.ID, TomcatManager.getInstance(), TomcatConstants.CONTEXT_XML_META_DATA);
  }
}
