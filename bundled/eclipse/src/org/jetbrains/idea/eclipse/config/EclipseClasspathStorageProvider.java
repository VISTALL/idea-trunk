package org.jetbrains.idea.eclipse.config;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.RootModelImpl;
import com.intellij.openapi.roots.impl.storage.ClasspathStorage;
import com.intellij.openapi.roots.impl.storage.ClasspathStorageProvider;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.eclipse.EclipseBundle;
import org.jetbrains.idea.eclipse.EclipseXml;
import org.jetbrains.idea.eclipse.IdeaXml;
import org.jetbrains.idea.eclipse.conversion.ConversionException;
import org.jetbrains.idea.eclipse.conversion.DotProjectFileHelper;
import org.jetbrains.idea.eclipse.conversion.EclipseClasspathReader;
import org.jetbrains.idea.eclipse.conversion.EclipseClasspathWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Vladislav.Kaznacheev
 */
public class EclipseClasspathStorageProvider implements ClasspathStorageProvider {
  @NonNls public static final String ID = "eclipse";
  public static final String DESCR = EclipseBundle.message("eclipse.classpath.storage.description");

  @NonNls
  public String getID() {
    return ID;
  }

  @Nls
  public String getDescription() {
    return DESCR;
  }

  public void assertCompatible(final ModifiableRootModel model) throws ConfigurationException {
    for (OrderEntry entry : model.getOrderEntries()) {
      if (entry instanceof LibraryOrderEntry && ((LibraryOrderEntry)entry).isModuleLevel()) {
        final Library library = ((LibraryOrderEntry)entry).getLibrary();
        if (library == null ||
            entry.getUrls(OrderRootType.CLASSES).length != 1 ||
            entry.getUrls(OrderRootType.SOURCES).length > 1 ||
            entry.getUrls(JavadocOrderRootType.getInstance()).length > 1 ||
            library.isJarDirectory(library.getUrls(OrderRootType.CLASSES)[0])) {
          throw new ConfigurationException(
            "Library \'" + entry.getPresentableName() + "\' is incompatible with eclipse format which supports only one content root");
        }
      }
    }

    if (model.getContentEntries().length != 1) {
      throw new ConfigurationException(EclipseBundle.message("eclipse.export.too.many.content.roots", model.getModule().getName()));
    }

    final String output = model.getModuleExtension(CompilerModuleExtension.class).getCompilerOutputUrl();
    final String contentRoot = model.getContentEntries()[0].getUrl();
    if (output == null || !StringUtil.startsWith(output, contentRoot)) {
      throw new ConfigurationException("Output path is incompatible with eclipse format which supports output under content root only");
    }
  }

  public void detach(Module module) {
    EclipseModuleManager.getInstance(module).setDocumentSet(null);
  }

  public ClasspathConverter createConverter(Module module) {
    return new EclipseClasspathConverter(module);
  }

  static void registerFiles(final CachedXmlDocumentSet fileCache, final Module module, final String moduleRoot, final String storageRoot) {
    fileCache.register(EclipseXml.CLASSPATH_FILE, storageRoot);
    fileCache.register(EclipseXml.PROJECT_FILE, storageRoot);
    fileCache.register(EclipseXml.PLUGIN_XML_FILE, storageRoot);
    fileCache.register(module.getName() + EclipseXml.IDEA_SETTINGS_POSTFIX, moduleRoot);
  }

  static CachedXmlDocumentSet getFileCache(final Module module) {
    CachedXmlDocumentSet fileCache = EclipseModuleManager.getInstance(module).getDocumentSet();
    if (fileCache == null) {
      fileCache = new CachedXmlDocumentSet(module.getProject());
      EclipseModuleManager.getInstance(module).setDocumentSet(fileCache);
      registerFiles(fileCache, module, ClasspathStorage.getModuleDir(module), ClasspathStorage.getStorageRootFromOptions(module));
      fileCache.preload();
    }
    return fileCache;
  }

  public void moduleRenamed(final Module module, String newName) {
    if (ClasspathStorage.getStorageType(module).equals(ID)) {
      try {
        final CachedXmlDocumentSet documentSet = getFileCache(module);


        final String oldEmlName = module.getName() + EclipseXml.IDEA_SETTINGS_POSTFIX;

        final String root = documentSet.getParent(oldEmlName);
        final File source = new File(root, oldEmlName);
        if (source.exists()) {
          final File target = new File(root, newName + EclipseXml.IDEA_SETTINGS_POSTFIX);
          FileUtil.rename(source, target);
          LocalFileSystem.getInstance().refreshAndFindFileByIoFile(target);
        }
        final CachedXmlDocumentSet fileCache = getFileCache(module);
        fileCache.delete(oldEmlName);
        fileCache.register(newName + EclipseXml.IDEA_SETTINGS_POSTFIX, ClasspathStorage.getModuleDir(module));
        fileCache.load(newName + EclipseXml.IDEA_SETTINGS_POSTFIX);
      }
      catch (IOException ignore) {
      }
      catch (JDOMException e) {

      }
    }
  }


  public static class EclipseClasspathConverter implements ClasspathConverter {

    private final Module module;

    public EclipseClasspathConverter(final Module module) {
      this.module = module;
    }

    public CachedXmlDocumentSet getFileSet() {
      CachedXmlDocumentSet fileCache = EclipseModuleManager.getInstance(module).getDocumentSet();
      return fileCache != null ? fileCache : getFileCache(module);
    }

    public Set<String> getClasspath(ModifiableRootModel model, final Element element) throws IOException, InvalidDataException {
      try {
        final HashSet<String> usedVariables = new HashSet<String>();
        final CachedXmlDocumentSet documentSet = getFileSet();
        final VirtualFile vFile = documentSet.getVFile(EclipseXml.PROJECT_FILE);
        assert vFile != null;
        final VirtualFile parent = vFile.getParent();
        assert parent != null;
        final String path = parent.getPath();

        final EclipseClasspathReader classpathReader = new EclipseClasspathReader(path, module.getProject(), null);
        classpathReader.init(model);
        if (documentSet.exists(EclipseXml.CLASSPATH_FILE)) {

          classpathReader.readClasspath(model, new ArrayList<String>(), new ArrayList<String>(), usedVariables, new HashSet<String>(), null,
                                        documentSet.read(EclipseXml.CLASSPATH_FILE).getRootElement());
          final String eml = model.getModule().getName() + EclipseXml.IDEA_SETTINGS_POSTFIX;
          if (documentSet.exists(eml)) {
            EclipseClasspathReader.readIDEASpecific(documentSet.read(eml).getRootElement(), model);
          } else {
            model.getModuleExtension(CompilerModuleExtension.class).setExcludeOutput(false);
          }
        }

        ((RootModelImpl)model).writeExternal(element);
        return usedVariables;
      }
      catch (ConversionException e) {
        throw new InvalidDataException(e);
      }
      catch (WriteExternalException e) {
        throw new InvalidDataException(e);
      }
      catch (JDOMException e) {
        throw new InvalidDataException(e);
      }
    }

    public void setClasspath(final ModifiableRootModel model) throws IOException, WriteExternalException {
      try {
        final Element classpathElement = new Element(EclipseXml.CLASSPATH_TAG);
        final EclipseClasspathWriter classpathWriter = new EclipseClasspathWriter(model);
        final CachedXmlDocumentSet fileSet = getFileSet();

        Element element;
        try {
          element = fileSet.read(EclipseXml.CLASSPATH_FILE).getRootElement();
        }
        catch (Exception e) {
          element = null;
        }

        if (element != null || model.getSourceRoots().length > 0) {
          classpathWriter.writeClasspath(classpathElement, element);
          fileSet.write(new Document(classpathElement), EclipseXml.CLASSPATH_FILE);
        }

        try {
          fileSet.read(EclipseXml.PROJECT_FILE);
        }
        catch (Exception e) {
          DotProjectFileHelper.saveDotProjectFile(module, fileSet.getParent(EclipseXml.PROJECT_FILE));
        }

        final Element ideaSpecific = new Element(IdeaXml.COMPONENT_TAG);
        final String emlFilename = model.getModule().getName() + EclipseXml.IDEA_SETTINGS_POSTFIX;
        if (classpathWriter.writeIDEASpecificClasspath(ideaSpecific)) {
          fileSet.write(new Document(ideaSpecific), emlFilename);
        }
        else {
          fileSet.delete(emlFilename);
        }
      }
      catch (ConversionException e) {
        throw new WriteExternalException(e.getMessage());
      }
    }
  }
}
