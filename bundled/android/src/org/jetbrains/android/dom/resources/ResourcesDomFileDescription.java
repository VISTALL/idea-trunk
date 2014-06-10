package org.jetbrains.android.dom.resources;

import com.intellij.util.xml.DomFileDescription;
import com.intellij.psi.xml.XmlFile;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.android.resourceManagers.ResourceManager;

/**
 * @author yole
 */
public class ResourcesDomFileDescription extends DomFileDescription<Resources> {
  public ResourcesDomFileDescription() {
    super(Resources.class, "resources", "values");
  }

  @Override
  public boolean isMyFile(@NotNull XmlFile file, @Nullable Module module) {
    return ResourceManager.isInResourceSubdirectory(file, "values");
  }
}
