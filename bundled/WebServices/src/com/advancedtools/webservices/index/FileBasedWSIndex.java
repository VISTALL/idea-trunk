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

package com.advancedtools.webservices.index;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.PsiUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * @by Konstantin Bulenkov
 */
public class FileBasedWSIndex extends FileBasedIndexExtension<String, WSIndexEntry> {
  private static final int VERSION = 3;
  
  public static final @NonNls String WS_TYPE = "Web service";
  public static final @NonNls String JAXB_TYPE = "JAXB mapped type";
  public static final @NonNls String WS_METHOD = "Web service method";
  public static final @NonNls String WS_PARAMETER_TYPE= "Web service parameter type";
  public static final @NonNls String WS_PARAMETER_PROPERTY_TYPE= "Web service parameter property";
  public static final @NonNls String JAXB_PROPERTY_TYPE= "JAXB mapped property";

  private static FileBasedWSIndex instance;
  private long modificationStamp = 0;

  public static final ID<String, WSIndexEntry> WS_INDEX = ID.create("FileBasedWSIndex");

  private final EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();

  @NonNls
  public static final String ANY_NAME = "*";

  public ID<String, WSIndexEntry> getName() {
    return WS_INDEX;
  }

  public FileBasedWSIndex() {
    if (instance == null) setInstance(this);
  }

  public static FileBasedWSIndex getInstance() {
    return instance;
  }

  static void setInstance(@NotNull FileBasedWSIndex inst) {
    instance = inst;
  }

  public DataIndexer<String, WSIndexEntry, FileContent> getIndexer() {
    return new DataIndexer<String, WSIndexEntry, FileContent>() {
      @NotNull
      public Map<String, WSIndexEntry> map(final FileContent inputData) {
        Map<String, WSIndexEntry> map = new HashMap<String, WSIndexEntry>();
        try {
          WSIndexEntry entry = new WSIndexEntry(inputData);
          for (String candidate : entry.myCandidates) {
            map.put(candidate, entry);
          }
          entry.myCandidates.clear();
          if (map.size() > 0) modificationStamp++;
        }
        catch (IOException e) {//
        }
        return map;
      }
    };
  }

  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  private final DataExternalizer<WSIndexEntry> myValueExternalizer = new DataExternalizer<WSIndexEntry>() {
      public void save(final DataOutput out, final WSIndexEntry value) throws IOException {
        if (value != null) {
          value.write(out);
        }
      }

      @Nullable
      public WSIndexEntry read(final DataInput in) throws IOException {
        try {
          return new WSIndexEntry(in);
        } catch (IOException ex) {
          return null;
        }
      }
    };


  public DataExternalizer<WSIndexEntry> getValueExternalizer() {
    return myValueExternalizer;
  }

  public FileBasedIndex.InputFilter getInputFilter() {
    return myInputFilter;
  }

  public boolean dependsOnFileContent() {
    return true;
  }

  public int getVersion() {
    return VERSION;
  }

  private final FileBasedIndex.InputFilter myInputFilter = new FileBasedIndex.InputFilter() {
    public boolean acceptInput(final VirtualFile vf) {
      return isAcceptableFile(vf); //TODO check file in Project
    }
  };

  static final @NonNls String JAVA_CLASS_EXTENTION = "class";
  static final @NonNls String JAVA_SOURCE_EXTENTION = "java";

  static boolean isAcceptableFile(final VirtualFile vf) {
    final String extension = vf.getExtension();
    if (JAVA_CLASS_EXTENTION.equals(extension) || JAVA_SOURCE_EXTENTION.equals(extension)) {
      return false;
    }

    if (! (vf.getFileSystem() instanceof LocalFileSystem)) return false;

    boolean wsdd = WebServicesPluginSettings.WSDD_FILE_EXTENSION.equals(extension);
    boolean wsdl = WebServicesPluginSettings.WSDL_FILE_EXTENSION.equals(extension);
    boolean jaxb = WebServicesPluginSettings.XSD_FILE_EXTENSION.equals(extension);
    boolean xfireServices = isXFireWs(vf);
    boolean jaxwsServices = isSunJaxWs(vf);
    boolean jaxrpc = isJaxRpc(vf);
    boolean jaxrpc2 = isJaxRpc2(vf);
    boolean cxf = isCxf(vf);

    return wsdd || wsdl || jaxb || xfireServices || jaxwsServices || jaxrpc || jaxrpc2 || cxf;
  }

  static boolean isCxf(VirtualFile vf) {
    return isFileLike(WebServicesPluginSettings.CXF_SERVLET_XML, vf);
  }

  static boolean isJaxRpc2(VirtualFile vf) {
    return isFileLike(WebServicesPluginSettings.JAXRPC_RI_RUNTIME_XML, vf);
  }

  private static boolean isFileLike(String pattern, VirtualFile vf) {
    boolean b = pattern.equals(vf.getName());

    if (!b && ApplicationManager.getApplication().isUnitTestMode()) {
      final int ind = pattern.lastIndexOf('.');
      final String name = vf.getName();
      final int ind2 = name.lastIndexOf('.');
      b = name.regionMatches(0, pattern, 0, ind)
       && name.regionMatches(ind2+1, pattern, ind+1, pattern.length() - ind - 1);      
    }
    return b;
  }

  static boolean isJaxRpc(VirtualFile vf) {
    return isFileLike(WebServicesPluginSettings.JAXRPC_XML, vf);
  }

  static boolean isSunJaxWs(VirtualFile vf) {
    return isFileLike(WebServicesPluginSettings.SUN_JAXWS_XML, vf);
  }

  static boolean isXFireWs(VirtualFile vf) {
    return isFileLike(WebServicesPluginSettings.XFIRE_SERVICES_XML, vf);
  }

  @Nullable
  public static String getKey(PsiMember c) {
    PsiClass containingClass = c.getContainingClass();
    return c instanceof PsiClass ?
           ((PsiClass)c).getQualifiedName()
           :
           containingClass == null ? null : containingClass.getQualifiedName() + "."+ c.getName();
  }

  public @NotNull synchronized WSIndexEntry[] getWsEntries(@NotNull Collection<WSIndexEntry> entries, PsiMember c) {
    final String qname = getKey(c);
    final PsiModifierList modifierList = c.getModifierList();
    final String anyqname = modifierList != null ? modifierList.hasModifierProperty(PsiModifier.PUBLIC)? getAnyKey(c):null:null;
    List<WSIndexEntry> result = null;

    final boolean fileLevel = c.getParent() instanceof PsiFile;
    Set<Module> depedentModules = null;

    final ProjectFileIndex index = ProjectRootManager.getInstance(c.getProject()).getFileIndex();

    if (fileLevel) {
      final VirtualFile vf = c.getContainingFile().getVirtualFile();

      if (vf == null) return WSIndexEntry.EMPTY;

      final Module moduleForFile = index.getModuleForFile(vf);

      if (moduleForFile != null) {
        depedentModules = new THashSet<Module>();
        depedentModules.add(moduleForFile);
        final Module[] modules = ModuleRootManager.getInstance(moduleForFile).getDependencies();
        depedentModules.addAll(Arrays.asList(modules));
      }
    }

    for(WSIndexEntry entry:entries) {
      if (fileLevel) {
        final VirtualFile file = entry.getFile();
        if (depedentModules != null && (file == null || !depedentModules.contains(index.getModuleForFile(file)))) continue;
      }

      result = addEntry(entry, qname, result);

      if (anyqname != null) {
        result = addEntry(entry, anyqname, result);
      }
    }

    return result != null ? result.toArray(new WSIndexEntry[result.size()]): WSIndexEntry.EMPTY;
  }

  private static List<WSIndexEntry> addEntry(WSIndexEntry entry, String qname, List<WSIndexEntry> result) {
    if (entry.mySymbols.contains(qname)) {
      WSTextRange range = entry.myLinks.get(qname);
      if (result == null) result = new LinkedList<WSIndexEntry>();
      if (range != null) result.add(entry);
    }
    return result;
  }

  public synchronized @NotNull WSIndexEntry[] getWsEntries(PsiClass c) {
    String key = c.getName();
    final Module module = PsiUtil.findModule(c);
    if (module == null || key == null) return WSIndexEntry.EMPTY;
    List<WSIndexEntry> values = FileBasedIndex.getInstance().getValues(WS_INDEX, key, GlobalSearchScope.moduleScope(module));    
    while(values.remove(null));
    Set<WSIndexEntry> entries = new THashSet<WSIndexEntry>();
    for (WSIndexEntry entry : values) {
      if (! entry.isResolved(module.getProject())) entry.resolve(c.getProject());
      if (entry.mySymbols.contains(c.getQualifiedName())) {
        entries.add(entry);
      }
    }

    return entries.isEmpty() ? WSIndexEntry.EMPTY : entries.toArray(new WSIndexEntry[entries.size()]);
  }

  private static final HashMap<Project, ProjectBasedFileFilter> filters = new HashMap<Project, ProjectBasedFileFilter>();
  static ProjectBasedFileFilter findFilter(Project project) {
    if (filters.get(project) == null) {
      filters.put(project, new ProjectBasedFileFilter(project));
    }
    return filters.get(project);
  }


  @Nullable
  static String getAnyKey(PsiMember member) {
    final PsiClass aClass = member instanceof PsiMethod ? member.getContainingClass():null;

    if (aClass != null) {
      return aClass.getQualifiedName() + "." + ANY_NAME;
    }
    return null;
  }

  public long getModificationStamp() {
    return modificationStamp;
  }
}

class WSIndexKey {
  public static final @NonNls String SEPARATOR = "~";
  private final String key;

  WSIndexKey(String fqn, String projectHash) {
    key = fqn + SEPARATOR + projectHash;
  }

  WSIndexKey(String key) {
    this.key = key;
  }

  public String toString() {
    return key;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return (obj instanceof String) && key.equals(obj.toString());
  }
}
