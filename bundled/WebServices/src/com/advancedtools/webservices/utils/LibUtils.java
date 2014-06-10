package com.advancedtools.webservices.utils;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WSLibrarySynchronizer;
import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.jaxb.JaxbMappingEngine;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.utils.facet.WebServicesClientLibraries;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.text.StringTokenizer;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author maxim
 * Date: 04.12.2004
 */
public class LibUtils {
  public static final @NonNls String PLUGIN_NAME="WebServices";
  private static final @NonNls String PLUGIN_NAME2="WebServices_demetra";

  public static final @NonNls String FILE_URL_PREFIX = "file:";

  private LibUtils() {}

  public static String[] getLibUrlsForToolRunning(ExternalEngine engine, final Module module) {
    if (WebServicesClientLibraries.isSupported(engine.getName())) {
      LibraryInfo[] info = WebServicesClientLibraries.getNecessaryLibraries(engine.getName());
      Set<String> libs = new THashSet<String>();
      libs.addAll(WSLibrarySynchronizer.getAvailableJarPaths(engine.getName()));
      for (LibraryInfo lib : info) {
        String[] classes = lib.getRequiredClasses();
        if (classes.length == 0) continue;
        String[] libraries = getLibUrlsForToolRunning(classes[0], module.getProject());
        for (String library : libraries) libs.add(library);
      }
      if (CompilerModuleExtension.getInstance(module).getCompilerOutputPath() != null) {
        libs.add(CompilerModuleExtension.getInstance(module).getCompilerOutputPath().getPath());
      }
      return libs.toArray(new String[libs.size()]);   
    } else {
    return getLibsUrlsFromLibInfos(engine.getLibraryDescriptors(new ExternalEngine.LibraryDescriptorContext() {
      public boolean isForRunningGeneratedCode() {
        return false;
      }

      public String getBindingType() {
        return null;
      }

      public Module getTargetModule() {
        return module;
      }
    }), engine.getBasePath());
    }
  }

  public static String[] getLibUrlsForToolRunning(String fqn, final Project project) {
    Library lib = LibraryUtil.findLibraryByClass(fqn, project);
    if (lib == null) {
      VirtualFile[] roots = LibraryUtil.getLibraryRoots(project);
      VirtualFile[] files = new VirtualFile[1];
      for (VirtualFile root : roots) {
        files[0] = root;
        if (LibraryUtil.isClassAvailableInLibrary(files, fqn)) {
          return new String[]{files[0].getPresentableUrl()};
        }
      }
      return ArrayUtil.EMPTY_STRING_ARRAY;
    } else {
      VirtualFile[] files = lib.getFiles(OrderRootType.COMPILATION_CLASSES);
      String[] jars = new String[files.length];
      for (int i = 0; i < files.length; i++) {
        jars[i] = files[i].getPresentableUrl();
      }
      return jars;
    }
  }

  @Nullable
  public static String getLibUrlByName(@NotNull String name, final Module module) {
    VirtualFile[] jars = LibraryUtil.getLibraryRoots(new Module[]{module}, false, false);
    for (VirtualFile jar : jars) {
      if (name.equals(jar.getName())) return jar.getPresentableUrl();
    }
    return null;
  }

  public static void setupLibsForGeneratedCode(Module module, ExternalEngine engine, final String bindingType) {
    String libPath = detectLibPath(engine);
    setupLibraries(module, getGeneratedCodeLibInfos(engine, bindingType, module), libPath, false);
  }

  public static ExternalEngine.LibraryDescriptor[] getGeneratedCodeLibInfos(ExternalEngine engine, final String bindingType, final Module module) {
    return engine.getLibraryDescriptors(new ExternalEngine.LibraryDescriptorContext() {
      public boolean isForRunningGeneratedCode() {
        return true;
      }

      public String getBindingType() {
        return bindingType;
      }

      public Module getTargetModule() {
        return module;
      }
    });
  }

  static void addRequiredLibraryIfNeeded(final LibraryTable libraryTable, final ExternalEngine.LibraryDescriptor info, final String libPath, final ModifiableRootModel rootModel) {
    final String name = info.getName();
    Library library = null;

    if (name != null) {
      library = libraryTable.getLibraryByName(name);
    } else {
      Iterator libraryIterator = libraryTable.getLibraryIterator();
      String firstJarName = info.getLibJars()[0];

      Out:
      while(libraryIterator.hasNext()) {
        final Library lib = (Library) libraryIterator.next();
        if (lib.getName() != null) continue;

        for(VirtualFile f:lib.getFiles(OrderRootType.CLASSES)) {
          if (firstJarName.equals(f.getPresentableName())) {
            library = lib;
            break Out;
          }
        }
      }
    }

    if (library==null) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          Library library2 = name != null ? libraryTable.createLibrary(name):libraryTable.createLibrary();
          final Library.ModifiableModel model = library2.getModifiableModel();
          for(String jar:info.getLibJars()) {
            addOneJar(jar, model, libPath);
          }

          model.commit();
          if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryTable.getTableLevel())) {
            final LibraryOrderEntry orderEntry = rootModel.addLibraryEntry(library2);
            rearrageEntries(rootModel, orderEntry);
          }
        }
      });
    } else if (noReferenceToLibrary(rootModel, library)) {
      final Library library1 = library;
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          final LibraryOrderEntry orderEntry = rootModel.addLibraryEntry(library1);
          rearrageEntries(rootModel, orderEntry);
        }
      });
    }
  }

  // Ensure our added library is the first in classpath (since it can override JDK's or JavaEE classes)
  private static void rearrageEntries(final ModifiableRootModel rootModel, final LibraryOrderEntry orderEntry) {
    final OrderEntry[] orderEntries = rootModel.getOrderEntries();
    final OrderEntry[] newOrderEntries = new OrderEntry[orderEntries.length];
    newOrderEntries[0] = orderEntry;

    int index = 1;
    for(int i = 0; i < orderEntries.length; ++i) {
      if (orderEntries[i] == orderEntry) continue;
      newOrderEntries[index ++] = orderEntries[i];
    }

    rootModel.rearrangeOrderEntries(newOrderEntries);
  }

  private static boolean noReferenceToLibrary(ModifiableRootModel rootModel, Library library) {
    LibraryOrderEntry libraryOrderEntry = rootModel.findLibraryOrderEntry(library);
    if (libraryOrderEntry != null) return false;

    for(OrderEntry o:rootModel.getOrderEntries()) {
      if (o instanceof LibraryOrderEntry) {
        final LibraryOrderEntry orderEntry = (LibraryOrderEntry) o;
        final String name = orderEntry.getLibraryName();

        if (name == null) continue;
        if (name.equals(library.getName())) {
          return false;
        }
      }
    }
    return true;
  }

  private static void addOneJar(String jarFileName, Library.ModifiableModel model, String libPath) {
    File file = new File( (libPath + jarFileName).replace('\\','/') );
    VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(file);
    if (fileByIoFile == null) {
      fileByIoFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
    if (fileByIoFile != null) {
      VirtualFile fileByPath = JarFileSystem.getInstance().findFileByPath(fileByIoFile.getPath() + JarFileSystem.JAR_SEPARATOR);
      model.addRoot(fileByPath, OrderRootType.CLASSES);
    }
  }

  public static String detectPluginPath() {
    String s = PathManager.getPluginsPath() + "/" + PLUGIN_NAME;

    if (!new File(s).exists()) {
      s = PathManager.getPluginsPath() + "/" + PLUGIN_NAME2;

      if (!new File(s).exists()) {
        s = PathManager.getPreinstalledPluginsPath() + "/" + PLUGIN_NAME;

        if (!new File(s).exists()) {
          s = PathManager.getPreinstalledPluginsPath() + "/" + PLUGIN_NAME2;

          if (!new File(s).exists()) {
            String homeWhenCompiledFromIdea = PathManager.getResourceRoot(WSBundle.class, "/com/advancedtools/webservices/WSBundle.properties");
            if (homeWhenCompiledFromIdea != null) return homeWhenCompiledFromIdea;

            throw new RuntimeException("Plugin home is not found");
          }
        }
      }
    }

    return s;
  }

  static String detectLibPath(@NotNull ExternalEngine engine) {
    final String s = engine.getBasePath();
    if (s == null && (engine instanceof JWSDPWSEngine || engine instanceof JaxbMappingEngine || engine instanceof RestWSEngine || engine instanceof AxisWSEngine)) {
      return null;  // classes could be in JDK
    }

    if (!new File(s).exists()) {
      throw new RuntimeException("Required libs are missed");
    }
    return s + File.separatorChar;
  }

  public static String detectDocPath() {
    String pluginPath = detectPluginPath();
    String s = pluginPath + "/docs";
    if (!new File(s).exists()) {
      throw new RuntimeException("Required docs are missed at "+s);
    }
    return s + "/";
  }

  public static String[] getLibsUrlsFromLibInfos(ExternalEngine.LibraryDescriptor[] libInfos, String basePath) {
    List<String> result = new ArrayList<String>();

    for (int i = 0; i < libInfos.length; i++) {
      for(String s:libInfos[i].getLibJars()) {
        result.add(basePath + File.separator + s);
      }
    }

    return result.toArray(new String[result.size()]);
  }

  public static void setupLibraries(Module currentModule, ExternalEngine.LibraryDescriptor[] libInfos, String libPath, boolean moduleLibrary) {
    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(currentModule).getModifiableModel();
    LibraryTable libraryTable = (moduleLibrary)?modifiableModel.getModuleLibraryTable(): LibraryTablesRegistrar.getInstance().getLibraryTable(currentModule.getProject());

    for (int i = 0; i < libInfos.length; i++) {
      addRequiredLibraryIfNeeded(libraryTable, libInfos[i],libPath, modifiableModel);
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        try {
          modifiableModel.commit();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public static XmlTag findServletWithName(XmlTag[] tags, @NotNull String axisServletName) {
    for (XmlTag tag : tags) {
      XmlTag[] subTags = tag.findSubTags("servlet-name");
      if (subTags.length == 0) continue;
      XmlTag name = subTags[0];
      String stringValue = getStringValue(name);

      if (stringValue.equals(axisServletName)) {
        return tag;
      }
    }
    return null;
  }

  public static String getStringValue(XmlTag name) {
    String stringValue = "";
    Object value = name.getValue();

    if (value instanceof String) {
      stringValue = (String) value;
    } else if (value instanceof XmlTagValue){
      stringValue = ((XmlTagValue)value).getTrimmedText();
    }
    return stringValue.trim();
  }

  @Nullable
  public static Module findModuleByOutputPath(Project project, String output) {
    Module[] modules = ModuleManager.getInstance(project).getModules();
    Module currentModule = null;

    Out:
    for (Module module : modules) {
      final VirtualFile[] urls = ModuleRootManager.getInstance(module).getFiles(OrderRootType.SOURCES);

      for (VirtualFile url : urls) {
        if (url.getPresentableUrl().equals(output)) {
          currentModule = module;
          break Out;
        }
      }
    }
    return currentModule;
  }

  public static File saveSourceGeneratedFile(String wsdlUrl, String outputPath, @Nullable String packagePrefix) throws IOException {
    final URL url = new URL(wsdlUrl);
    InputStream stream = url.openStream();

    final int i = wsdlUrl.lastIndexOf('/');
    final int i2 = wsdlUrl.indexOf('?',i);
    String fileName = wsdlUrl.substring(i,i2 != -1 ? i2:wsdlUrl.length());
    if (i2 != -1 && !fileName.endsWith(".wsdl") && fileName.indexOf('.') == -1) {
      fileName += ".wsdl";
    }

    final String s = outputPath + (packagePrefix != null ? "/" + packagePrefix.replace('.', File.separatorChar):"");
    File f = new File(s);
    f.mkdirs();

    String fullFileName = s + fileName;
    final File file = isSameFile(url, fullFileName) ?
                      new File(fullFileName) : FileUtils.saveStreamContentAsFile(fullFileName, stream);
    scanFile(file, new FileProcessor() {
      final Pattern p = Pattern.compile("(?:(?:location|schemaLocation)\\s*=\\s*(\"|\'))([^\"\']*)\\1");

      public void fileScanningEnded() throws IOException {
      }

      public boolean process(String s) throws IOException {
        if (s.indexOf("location") != -1 || s.indexOf("schemaLocation") != -1) {
          final Matcher matcher = p.matcher(s);
          final StringBuffer result = new StringBuffer();
          while(matcher.find()) {}
          matcher.appendTail(result);
        }
        return true;
      }
    });
    return file;
  }

  private static boolean isSameFile(final URL url, final String fullFileName) {
    try {
      return new File(fullFileName).equals(new File(url.toURI()));
    } catch (Exception e) {
      return false;
    }
  }

  public static Module getModuleFromClass(PsiClass aClass) {
    if (aClass != null) {
      final PsiFile psiFile = aClass.getContainingFile();
      return ProjectRootManager.getInstance(psiFile.getProject()).getFileIndex().getModuleForFile(psiFile.getVirtualFile());
    }
    return null;
  }

  public static String findOutputDir(Module module) {
    final VirtualFile[] urls = ModuleRootManager.getInstance(module).getFiles(OrderRootType.SOURCES);
    String generateSourcesDir = null;

    for (VirtualFile url : urls) {
      if (url.getExtension() == null && url.isWritable()) {
        String presentableUrl = url.getPresentableUrl();
        generateSourcesDir = presentableUrl;
        break;
      }
    }
    return generateSourcesDir;
  }

  public static void doFileSystemRefresh() {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        VirtualFileManager.getInstance().refresh(false);
      }
    });
  }

  public static void scanFile(File file, FileProcessor processor) throws IOException {
    LineNumberReader reader = new LineNumberReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));

    doScanFile(reader, processor);
  }

  public static void doScanFile(LineNumberReader reader, FileProcessor processor) throws IOException {
    String s = reader.readLine();

    while(s != null) {
      if (!processor.process(s)) {
        break;
      }
      s = reader.readLine();
    }

    reader.close();
    processor.fileScanningEnded();
  }

  public static String retrieveTargetNamespace(File savedWsdlFile) throws InvokeExternalCodeUtil.ExternalCodeException {
    final String[] detectedNsFromWsdl = new String[1];

    try {
      scanFile(savedWsdlFile, new FileProcessor() {
        final Pattern p = Pattern.compile("(?:(?:namespace|targetNamespace)\\s*=\\s*(\"|\'))([^\"\']*)\\1");
        public void fileScanningEnded() throws IOException {}

        public boolean process(String s) throws IOException {
          if (s.indexOf("namespace=\"") != -1 || s.indexOf("targetNamespace=\"") != -1) {
            final Matcher matcher = p.matcher(s);
            if (matcher.find() && detectedNsFromWsdl[0] == null) {
              detectedNsFromWsdl[0] = matcher.group(2);
            }
          }
          return true;
        }
      });
    } catch (IOException e) {
      throw new InvokeExternalCodeUtil.ExternalCodeException(e);
    }
    return detectedNsFromWsdl[0];
  }

  public static boolean accessingLibraryJarsFromPluginBundledLibs(String basePath) {
    return !new File(basePath + File.separatorChar + "lib").exists();
  }

  static InputStream getResourcesStream(final String templateName) {
    return LibUtils.class.getResourceAsStream("/schemas/resources/" + templateName);
  }

  public static String getExtractedResourcesWebServicesDir() {
    return PathManager.getSystemPath() + File.separatorChar + "webservices";
  }

    public static boolean containsClassInLibs(@NotNull Project project, @NotNull String fqn) {
    for (VirtualFile file : LibraryUtil.getLibraryRoots(project, false, false)) {
      if (findInFile(file, new com.intellij.util.text.StringTokenizer(fqn,"."))) return true;
    }
    return false;
  }

  private static boolean findInFile(VirtualFile file, final StringTokenizer tokenizer) {
    if (!tokenizer.hasMoreTokens()) return true;
    @NonNls StringBuilder name = new StringBuilder(tokenizer.nextToken());
    if (!tokenizer.hasMoreTokens()) {
      name.append(".class");
    }
    final VirtualFile child = file.findChild(name.toString());
    return child != null && findInFile(child, tokenizer);
  }

  public interface FileProcessor {
    void fileScanningEnded() throws IOException;
    boolean process(String s) throws IOException;
  }
}
