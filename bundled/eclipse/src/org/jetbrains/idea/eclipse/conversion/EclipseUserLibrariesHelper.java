/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * User: anna
 * Date: 11-Mar-2009
 */
package org.jetbrains.idea.eclipse.conversion;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EclipseUserLibrariesHelper {
  //private static final String ORG_ECLIPSE_JDT_CORE_PREFS = "org.eclipse.jdt.core.prefs";
  //private static final String ORG_ECLIPSE_JDT_CORE_USER_LIBRARY = "org.eclipse.jdt.core.userLibrary.";

  private EclipseUserLibrariesHelper() {
  }

  private static void writeUserLibrary(final Library library, final Element libElement) {
    final VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
    for (VirtualFile file : files) {
      Element archElement = new Element("archive");
      if (file.getFileSystem() instanceof JarFileSystem) {
        final VirtualFile localFile = JarFileSystem.getInstance().getVirtualFileForJar(file);
        if (localFile != null) {
          file = localFile;
        }
      }
      archElement.setAttribute("path", file.getPath());
      libElement.addContent(archElement);
    }
  }

  public static void appendProjectLibraries(final Project project, @Nullable final File userLibrariesFile) throws IOException {
    if (userLibrariesFile == null) return;
    if (userLibrariesFile.exists() && !userLibrariesFile.isFile()) return;
    final File parentFile = userLibrariesFile.getParentFile();
    if (parentFile == null) return;
    if (!parentFile.isDirectory()) {
      if (!parentFile.mkdir()) return;
    }
    final Element userLibsElement = new Element("eclipse-userlibraries");
    final List<Library> libraries = new ArrayList<Library>(Arrays.asList(ProjectLibraryTable.getInstance(project).getLibraries()));
    libraries.addAll(Arrays.asList(LibraryTablesRegistrar.getInstance().getLibraryTable().getLibraries()));
    for (Library library : libraries) {
      Element libElement = new Element("library");
      libElement.setAttribute("name", library.getName());
      writeUserLibrary(library, libElement);
      userLibsElement.addContent(libElement);
    }
    JDOMUtil.writeDocument(new Document(userLibsElement), userLibrariesFile, "\n");
  }


  public static void readProjectLibrariesContent(File exportedFile, Project project, Collection<String> unknownLibraries)
    throws IOException, JDOMException {
    if (exportedFile.exists()) {
      final LibraryTable libraryTable = ProjectLibraryTable.getInstance(project);
      final Element rootElement = JDOMUtil.loadDocument(exportedFile).getRootElement();
      for (Object o : rootElement.getChildren("library")) {
        final Element libElement = (Element)o;
        final String libName = libElement.getAttributeValue("name");
        Library libraryByName = libraryTable.getLibraryByName(libName);
        if (libraryByName == null) {
          final LibraryTable.ModifiableModel model = libraryTable.getModifiableModel();
          libraryByName = model.createLibrary(libName);
          model.commit();
        }
        if (libraryByName != null) {
          final Library.ModifiableModel model = libraryByName.getModifiableModel();
          for (Object a : libElement.getChildren("archive")) {
            String rootPath = ((Element)a).getAttributeValue("path");
            if (rootPath.startsWith("/")) { //relative to workspace root
              rootPath = project.getBaseDir().getPath() + rootPath;
            }
            String url = VfsUtil.pathToUrl(rootPath);
            final VirtualFile localFile = VirtualFileManager.getInstance().findFileByUrl(url);
            if (localFile != null) {
              final VirtualFile jarFile = JarFileSystem.getInstance().getJarRootForLocalFile(localFile);
              if (jarFile != null) {
                url = jarFile.getUrl();
              }
            }
            model.addRoot(url, OrderRootType.CLASSES);
          }
          model.commit();
        }
        unknownLibraries.remove(libName);  //ignore finally found libraries
      }
    }
  }
}