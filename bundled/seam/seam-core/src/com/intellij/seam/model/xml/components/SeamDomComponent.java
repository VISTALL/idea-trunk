package com.intellij.seam.model.xml.components;

import com.intellij.seam.constants.SeamNamespaceConstants;
import com.intellij.seam.model.CommonSeamComponent;
import com.intellij.util.xml.Namespace;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Namespace(SeamNamespaceConstants.COMPONENTS_NAMESPACE_KEY)
public interface SeamDomComponent extends CommonSeamComponent, BasicSeamComponent, SeamEjbComponent {

  @NotNull
  List<SeamProperty> getProperties();
 }
