package org.jetbrains.android.resourceManagers;

import com.android.sdklib.SdkConstants;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.*;
import com.intellij.util.ArrayUtil;
import static com.intellij.util.ArrayUtil.contains;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.android.dom.attrs.AttributeDefinitions;
import org.jetbrains.android.dom.resources.Item;
import org.jetbrains.android.dom.resources.ResourceElement;
import org.jetbrains.android.dom.resources.Resources;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import static java.util.Collections.addAll;

/**
 * @author coyote
 */
public abstract class ResourceManager {
  public static final Set<String> REFERABLE_RESOURCE_TYPES = new HashSet<String>();
  public static final String[] FILE_RESOURCE_TYPES =
    new String[]{"drawable", "anim", "layout", "values", "menu", "xml", "raw", "color"};
  public static final String[] VALUE_RESOURCE_TYPES = new String[]{"drawable", "dimen", "color", "string", "style", "array", "id"};
  private static final String[] DRAWABLE_EXTENSIONS = new String[]{"png", "jpg", "gif"};

  protected final Module myModule;

  static {
    addAll(REFERABLE_RESOURCE_TYPES, FILE_RESOURCE_TYPES);
    addAll(REFERABLE_RESOURCE_TYPES, VALUE_RESOURCE_TYPES);
    REFERABLE_RESOURCE_TYPES.remove("values");
  }

  protected ResourceManager(@NotNull Module module) {
    myModule = module;
  }

  public Module getModule() {
    return myModule;
  }

  @Nullable
  public static String getDefaultResourceFileName(@NotNull String resourceType) {
    if (!ArrayUtil.contains(resourceType, VALUE_RESOURCE_TYPES)) {
      return null;
    }
    return resourceType + "s.xml";
  }

  @Nullable
  public abstract VirtualFile getResourceDir();

  @Nullable
  private static String getResourceTypeByDirName(@NotNull String name) {
    int index = name.indexOf('-');
    String type = index >= 0 ? name.substring(0, index) : name;
    return contains(type, FILE_RESOURCE_TYPES) ? type : null;
  }

  @NotNull
  protected List<VirtualFile> getResourceSubdirs(@Nullable String resourceType) {
    List<VirtualFile> dirs = new ArrayList<VirtualFile>();
    if (!contains(resourceType, FILE_RESOURCE_TYPES)) {
      return dirs;
    }
    VirtualFile resourcesDir = getResourceDir();
    if (resourcesDir == null) return dirs;
    if (resourceType == null) {
      dirs.addAll(Arrays.asList(resourcesDir.getChildren()));
      return dirs;
    }
    for (VirtualFile child : resourcesDir.getChildren()) {
      String type = getResourceTypeByDirName(child.getName());
      if (resourceType.equals(type)) dirs.add(child);
    }
    return dirs;
  }

  @Nullable
  private static String getResourceName(@NotNull String resourceType, @NotNull String fileName) {
    String extension = FileUtil.getExtension(fileName);
    String s = FileUtil.getNameWithoutExtension(fileName);
    if (resourceType.equals("drawable") && contains(extension, DRAWABLE_EXTENSIONS)) {
      if (s.endsWith(".9") && extension.equals("png")) {
        return s.substring(0, s.length() - 2);
      }
      return s;
    }
    return s;
  }

  private static boolean isCorrectFileName(@NotNull String resourceType, @NotNull String fileName) {
    return getResourceName(resourceType, fileName) != null;
  }

  private static boolean equal(@Nullable String s1, @Nullable String s2, boolean distinguishDelimeters) {
    if (s1 == null || s2 == null) {
      return false;
    }
    if (s1.length() != s2.length()) return false;
    for (int i = 0, n = s1.length(); i < n; i++) {
      char c1 = s1.charAt(i);
      char c2 = s2.charAt(i);
      if (distinguishDelimeters || (Character.isLetterOrDigit(c1) && Character.isLetterOrDigit(c2))) {
        if (c1 != c2) return false;
      }
    }
    return true;
  }

  @NotNull
  public List<PsiFile> findResourceFiles(@NotNull String resType,
                                         @Nullable String resName,
                                         boolean distinguishDelimetersInName,
                                         @NotNull String... extensions) {
    List<PsiFile> result = new ArrayList<PsiFile>();
    Set<String> extensionSet = new HashSet<String>();
    addAll(extensionSet, extensions);
    for (VirtualFile dir : getResourceSubdirs(resType)) {
      for (final VirtualFile resFile : dir.getChildren()) {
        String extension = resFile.getExtension();
        if (extensions.length == 0 || extensionSet.contains(extension)) {
          String s = getResourceName(resType, resFile.getName());
          if (resName == null || equal(resName, s, distinguishDelimetersInName)) {
            result.add(ApplicationManager.getApplication().runReadAction(new Computable<PsiFile>() {
              @Nullable
              public PsiFile compute() {
                return PsiManager.getInstance(myModule.getProject()).findFile(resFile);
              }
            }));
          }
        }
      }
    }
    return result;
  }

  public List<PsiFile> findResourceFiles(@NotNull String resType, @NotNull String resName, @NotNull String... extensions) {
    return findResourceFiles(resType, resName, true, extensions);
  }

  @NotNull
  public List<PsiFile> findResourceFiles(@NotNull String resType) {
    return findResourceFiles(resType, null, true);
  }

  @Nullable
  public String getValueResourceType(@NotNull XmlTag tag) {
    String fileResType = getFileResourceType(tag.getContainingFile());
    if ("values".equals(fileResType)) {
      return tag.getName();
    }
    return null;
  }

  @Nullable
  public String getFileResourceType(@NotNull PsiFile file) {
    PsiDirectory dir = file.getContainingDirectory();
    if (dir == null) return null;
    VirtualFile resDir = getResourceDir();
    PsiDirectory possibleResDir = dir.getParentDirectory();
    if (possibleResDir == null || !possibleResDir.getVirtualFile().equals(resDir)) {
      return null;
    }
    String type = getResourceTypeByDirName(dir.getName());
    if (type == null) return null;
    return isCorrectFileName(type, file.getName()) ? type : null;
  }

  @NotNull
  public Set<String> getFileResourcesNames(@NotNull String resourceType) {
    Set<String> result = new HashSet<String>();
    List<VirtualFile> dirs = getResourceSubdirs(resourceType);
    for (VirtualFile dir : dirs) {
      for (VirtualFile resourceFile : dir.getChildren()) {
        if (resourceFile.isDirectory()) continue;
        String resName = getResourceName(resourceType, resourceFile.getName());
        if (resName != null) result.add(resName);
      }
    }
    return result;
  }

  @NotNull
  public <T extends DomElement> List<T> getRootDomElements(@NotNull String resType, Class<T> elementType) {
    List<T> result = new ArrayList<T>();
    for (VirtualFile valueResourceDir : getResourceSubdirs(resType)) {
      for (VirtualFile valueResourceFile : valueResourceDir.getChildren()) {
        if (!valueResourceFile.isDirectory() && valueResourceFile.getFileType().equals(StdFileTypes.XML)) {
          T element = AndroidUtils.loadDomElement(myModule, valueResourceFile, elementType);
          if (element != null) result.add(element);
        }
      }
    }
    return result;
  }

  @NotNull
  public List<ResourceElement> getValueResources(@NotNull String resourceType) {
    List<ResourceElement> result = new ArrayList<ResourceElement>();
    Collection<Resources> resourceFiles = getResourceElements();
    for (Resources res : resourceFiles) {
      if (resourceType.equals("string")) {
        result.addAll(res.getStrings());
      }
      if (resourceType.equals("drawable")) {
        result.addAll(res.getDrawables());
      }
      if (resourceType.equals("color")) {
        result.addAll(res.getColors());
      }
      if (resourceType.equals("dimen")) {
        result.addAll(res.getDimens());
      }
      if (resourceType.equals("style")) {
        result.addAll(res.getStyles());
      }
      if (resourceType.equals("array")) {
        result.addAll(res.getStringArrays());
      }
      for (Item item : res.getItems()) {
        String type = item.getType().getValue();
        if (resourceType.equals(type)) {
          result.add(item);
        }
      }
    }
    return result;
  }

  public List<Resources> getResourceElements() {
    return getRootDomElements("values", Resources.class);
  }

  @Nullable
  public abstract AttributeDefinitions getAttributeDefinitions();

  // searches only declarations such as "@+id/..."
  @Nullable
  public abstract List<XmlAttributeValue> findIdDeclarations(@NotNull String id);

  @NotNull
  public abstract Collection<String> getIds();

  public List<ResourceElement> findValueResources(@NotNull String resType, @NotNull String resName) {
    return findValueResources(resType, resName, true);
  }

  @NotNull
  public List<ResourceElement> findValueResources(@NotNull String resourceType,
                                                  @NotNull String resourceName,
                                                  boolean distinguishDelimetersInName) {
    List<ResourceElement> elements = new ArrayList<ResourceElement>();
    for (ResourceElement element : getValueResources(resourceType)) {
      GenericAttributeValue<String> name = element.getName();
      if (name != null && equal(resourceName, name.getValue(), distinguishDelimetersInName)) {
        elements.add(element);
      }
    }
    return elements;
  }

  public static boolean isInResourceSubdirectory(@NotNull PsiFile file, @Nullable String resourceType) {
    file = file.getOriginalFile();
    PsiDirectory dir = file.getContainingDirectory();
    if (dir == null) return false;
    return isResourceSubdirectory(dir, resourceType);
  }

  public static boolean isResourceSubdirectory(PsiDirectory dir, String resourceType) {
    if (resourceType != null && !dir.getName().startsWith(resourceType)) return false;
    dir = dir.getParent();
    if (dir == null) return false;
    if ("default".equals(dir.getName())) {
      dir = dir.getParentDirectory();
    }
    return dir != null && isResourceDirectory(dir);
  }

  public static boolean isResourceDirectory(PsiDirectory dir) {
    if (!"res".equals(dir.getName())) return false;
    dir = dir.getParent();
    if (dir != null) {
      if (dir.findFile(SdkConstants.FN_ANDROID_MANIFEST_XML) != null) {
        return true;
      }
      dir = dir.getParent();
      if (dir != null) {
        if (containsAndroidJar(dir)) return true;
        dir = dir.getParent();
        if (dir != null) {
          return containsAndroidJar(dir);
        }
      }
    }
    return false;
  }

  private static boolean containsAndroidJar(@NotNull PsiDirectory psiDirectory) {
    return psiDirectory.findFile(SdkConstants.FN_FRAMEWORK_LIBRARY) != null;
  }

  @NotNull
  public Map<String, List<XmlAttributeValue>> createIdMap() {
    Map<String, List<XmlAttributeValue>> result = new HashMap<String, List<XmlAttributeValue>>();
    fillIdMap("layout", result);
    fillIdMap("menu", result);
    return result;
  }

  private <T extends DomElement> void fillIdMap(@NotNull String resType, @NotNull Map<String, List<XmlAttributeValue>> result) {
    List<PsiFile> resFiles = findResourceFiles(resType);
    for (PsiFile resFile : resFiles) {
      if (resFile instanceof XmlFile) {
        XmlDocument document = ((XmlFile)resFile).getDocument();
        if (document != null) {
          XmlTag rootTag = document.getRootTag();
          if (rootTag != null) {
            fillMapRecursively(rootTag, result);
          }
        }
      }
    }
  }

  private static void fillMapRecursively(@NotNull XmlTag tag, Map<String, List<XmlAttributeValue>> result) {
    XmlAttribute idAttr = tag.getAttribute("id", SdkConstants.NS_RESOURCES);
    if (idAttr != null) {
      XmlAttributeValue idAttrValue = idAttr.getValueElement();
      if (idAttrValue != null) {
        String value = idAttrValue.getValue();
        if (value.startsWith(AndroidResourceUtil.NEW_ID_PREFIX)) {
          String id = AndroidResourceUtil.getResourceNameByReferenceText(value);
          if (id != null) {
            List<XmlAttributeValue> list = result.get(id);
            if (list == null) {
              list = new ArrayList<XmlAttributeValue>();
              result.put(id, list);
            }
            list.add(idAttrValue);
          }
        }
      }
    }
    for (XmlTag subtag : tag.getSubTags()) {
      fillMapRecursively(subtag, result);
    }
  }
}
