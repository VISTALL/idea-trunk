package com.intellij.seam.model.xml;

import com.intellij.psi.PsiType;
import com.intellij.seam.model.xml.components.*;
import com.intellij.util.xml.model.DomModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SeamDomModel extends DomModel<SeamComponents> {

  @NotNull
  List<SeamDomComponent> getSeamComponents();

  @NotNull
  List<SeamDomComponent> getSeamComponents(@NotNull PsiType psiType);

  @NotNull
  List<SeamDomComponent> getSeamComponents(@NotNull String componentName);

  @NotNull
  List<SeamImport> getImports();

  @NotNull
  List<SeamDomFactory> getFactories();

  @NotNull
  List<SeamEvent> getEvents();
}
