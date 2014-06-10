package org.jetbrains.android.resourceManagers;

import com.android.sdklib.IAndroidTarget;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.android.dom.attrs.AttributeDefinitions;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author coyote
 */
public class SystemResourceManager extends ResourceManager {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.android.resourceManagers.SystemResourceManager");
  private volatile AttributeDefinitions definitions;
  private volatile Map<String, List<XmlAttributeValue>> myIdMap;

  private final IAndroidTarget myTarget;

  public SystemResourceManager(@NotNull Module module, @NotNull IAndroidTarget target) {
    super(module);
    myTarget = target;
  }

  @Nullable
  public VirtualFile getResourceDir() {
    String resPath = myTarget.getPath(IAndroidTarget.RESOURCES);
    return LocalFileSystem.getInstance().findFileByPath(resPath);
  }

  @Nullable
  public static SystemResourceManager getInstance(@NotNull ConvertContext context) {
    AndroidFacet facet = AndroidFacet.getInstance(context);
    return facet != null ? facet.getSystemResourceManager() : null;
  }

  @Nullable
  public synchronized AttributeDefinitions getAttributeDefinitions() {
    if (definitions == null) {
      String attrsPath = myTarget.getPath(IAndroidTarget.ATTRIBUTES);
      String attrsManifestPath = myTarget.getPath(IAndroidTarget.MANIFEST_ATTRIBUTES);
      XmlFile[] files = findFiles(attrsPath, attrsManifestPath);
      if (files != null) {
        definitions = new AttributeDefinitions(files);
      }
    }
    return definitions;
  }

  @Nullable
  public List<XmlAttributeValue> findIdDeclarations(@NotNull String id) {
    if (myIdMap == null) {
      myIdMap = createIdMap();
    }
    return myIdMap.get(id);
  }

  @NotNull
  public Collection<String> getIds() {
    if (myIdMap == null) {
      myIdMap = createIdMap();
    }
    return myIdMap.keySet();
  }

  @Nullable
  private XmlFile[] findFiles(final String... paths) {
    XmlFile[] xmlFiles = new XmlFile[paths.length];
    for (int i = 0; i < paths.length; i++) {
      String path = paths[i];
      final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
      PsiFile psiFile = file != null ? ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {
        @Nullable
        public PsiFile compute() {
          return PsiManager.getInstance(myModule.getProject()).findFile(file);
        }
      }) : null;
      if (psiFile == null) {
        LOG.info("File " + path + " is not found");
        return null;
      }
      if (!(psiFile instanceof XmlFile)) {
        LOG.info("File " + path + "  is not an xml psiFile");
        return null;
      }
      xmlFiles[i] = (XmlFile)psiFile;
    }
    return xmlFiles;
  }
}
