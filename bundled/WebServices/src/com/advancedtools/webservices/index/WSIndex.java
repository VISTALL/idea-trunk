package com.advancedtools.webservices.index;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @by maxim
 */
public class WSIndex extends AbstractProjectComponent {
  public static String WS_TYPE = "Web service";
  public static String JAXB_TYPE = "JAXB mapped type";
  public static String WS_METHOD = "Web service method";
  public static String WS_PARAMETER_TYPE= "Web service parameter type";
  public static String WS_PARAMETER_PROPERTY_TYPE= "Web service parameter property";
  public static String JAXB_PROPERTY_TYPE= "JAXB mapped property";

  private final Set<String> myWsClasses = new THashSet<String>(25);
  private final Map <VirtualFile, IndexEntry> myFiles = new THashMap<VirtualFile,IndexEntry>();
  private final Set<VirtualFile> myDirtyFiles = new THashSet<VirtualFile>();

  private long myModificationStamp;
  private final Runnable myUpdateRunnable;

  private boolean myBulkUpdate;
  private static WSIndex myIndex;
  @NonNls
  static final String ANY_NAME = "*";

  public WSIndex(final Project project) {
    super(project);

    myUpdateRunnable = new Runnable() {
      public void run() {
        if (project.isDisposed()) return;
        ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
          indicator.setIndeterminate(true);
          indicator.setText("Enumerating wsdl/wsdd/jaxb documents");
        }

        myBulkUpdate = true;
        ProjectRootManager.getInstance(project).getFileIndex().iterateContent(
          new ContentIterator() {
            public boolean processFile(VirtualFile fileOrDir) {
              if (!fileOrDir.isDirectory()) processFileAdded(fileOrDir);
              return true;
            }
          }
        );

        myBulkUpdate = false;
        myUpdateNamesMap();

        if (indicator != null) {
          indicator.setIndeterminate(false);
          indicator.setFraction(1.0);
        }
      }
    };
  }

  public void projectOpened() {
    if (!myProject.isDefault()) {
      if (ApplicationManager.getApplication().isUnitTestMode() && !myProject.isOpen()) {
        myUpdateRunnable.run();
      } else {
        StartupManager.getInstance(myProject).registerStartupActivity(myUpdateRunnable);
      }

      final VirtualFileListener myFileListener = new VirtualFileAdapter() {
        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();

        public void contentsChanged(VirtualFileEvent event) {
          final VirtualFile file = event.getFile();

          if (isAcceptableFile(file)) {
            if (!isFileUnderProject(file)) return;
            processFileChanged(file);
          }
        }

        public void fileCreated(VirtualFileEvent event) {
          final VirtualFile file = event.getFile();
          if (!isFileUnderProject(file)) return;
          processFileAdded(file);
        }

        private boolean isFileUnderProject(VirtualFile file) {
          return myProject.isInitialized() && !fileIndex.isIgnored(file) && fileIndex.getContentRootForFile(file) != null;
        }

        public void beforeFileDeletion(VirtualFileEvent event) {
          final VirtualFile vfile = event.getFile();
          if (!isAcceptableFile(vfile)) return;

          processFileRemoved(vfile);
        }
      };
      VirtualFileManager.getInstance().addVirtualFileListener(myFileListener, myProject);

      ModuleRootListener myRootListener = new ModuleRootListener() {
        public void beforeRootsChange(ModuleRootEvent event) {
        }

        public void rootsChanged(ModuleRootEvent event) {
          StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
            public void run() {
              myFiles.clear();
              myUpdateRunnable.run();
            }
          });
        }
      };
      ProjectRootManager.getInstance(myProject).addModuleRootListener(myRootListener, myProject);

      PsiTreeChangeListener myChangeListener = new TreeChangeListener();
      PsiManager.getInstance(myProject).addPsiTreeChangeListener(myChangeListener, myProject);
    }
  }

  @NotNull
  @NonNls
  public String getComponentName() {
    return "WebServices.WSIndex";
  }

  private synchronized void myUpdateNamesMap() {
    if (myBulkUpdate) return;
    myWsClasses.clear();

    for(IndexEntry entry:myFiles.values()) {
      myWsClasses.addAll(entry.mySymbols);
    }
  }

  private boolean isAcceptableFile(@NotNull VirtualFile vf) {
    final String extension = vf.getExtension();
    boolean wsdd = WebServicesPluginSettings.WSDD_FILE_EXTENSION.equals(extension);
    boolean wsdl = WebServicesPluginSettings.WSDL_FILE_EXTENSION.equals(extension);
    boolean jaxb = false; // WebServicesPluginSettings.XSD_FILE_EXTENSION.equals(extension); // skip scanning schemas since it takes a while to load them TODO: we need to find work around for this!
    boolean xfireServices = isXFireWs(vf);
    boolean jaxwsServices = isSunJaxWs(vf);
    boolean jaxrpc = isJaxRpc(vf);
    boolean jaxrpc2 = isJaxRpc2(vf);
    boolean cxf = isCxf(vf);

    boolean b = wsdd || wsdl || jaxb || xfireServices || jaxwsServices || jaxrpc || jaxrpc2 || cxf;
    ProjectFileIndex index = ProjectRootManager.getInstance(myProject).getFileIndex();
    if (b && (index.isIgnored(vf) || index.getContentRootForFile(vf) == null)) {
      b = false;
    }
    return b;
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
      b = vf.getName().matches(pattern.replace(".","\\d*\\.")); 
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

  private void incrementModificationStamp() {
    myModificationStamp++;

    myUpdateNamesMap();
    updateGutterMarkers();
  }

  public void updateGutterMarkers() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        if (myProject == null || myProject.isDisposed()) return;
        DaemonCodeAnalyzer.getInstance(myProject).restart();
      }
    }, ModalityState.defaultModalityState());
  }

  private synchronized void processFileAdded(final XmlFile file) {
    VirtualFile virtualFile = file.getVirtualFile();
    if (virtualFile == null) return;

    if (isAcceptableFile(virtualFile)) {
      myDirtyFiles.add(virtualFile);
    }
  }

  private synchronized void processFileAdded(VirtualFile fileOrDir) {
    if (fileOrDir.isDirectory()) {
      for(VirtualFile file:fileOrDir.getChildren()) {
        processFileAdded(file);
      }
      return;
    }

    if (isAcceptableFile(fileOrDir)) {
      final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
      if (indicator != null) indicator.setText2(fileOrDir.getPresentableUrl());

      if (myBulkUpdate) {
        final PsiFile file = PsiManager.getInstance(myProject).findFile(fileOrDir);
        if (file instanceof XmlFile) {
          if (!myFiles.containsKey(fileOrDir)) {
            myFiles.put( fileOrDir, new IndexEntry((XmlFile) file) );
          }
        }
      } else {
        myDirtyFiles.add(fileOrDir);
      }
    }
  }

  private synchronized void processFileRemoved(VirtualFile virtualFile) {
    if (virtualFile == null) return;

    if (isAcceptableFile(virtualFile)) {
      myFiles.remove( virtualFile );
      myDirtyFiles.remove(virtualFile);
    }
  }

  private synchronized void processFileChanged(final VirtualFile virtualFile) {
    if (virtualFile == null) return;
    if (isAcceptableFile(virtualFile)) {
      myDirtyFiles.add(virtualFile);
    }
  }

  public static void setInstance(WSIndex instance) {
    assert ApplicationManager.getApplication().isUnitTestMode();
    myIndex = instance;
  }

  public static WSIndex getInstance(Project project) {
    WSIndex index = project.getComponent(WSIndex.class);
    if (index == null && ApplicationManager.getApplication().isUnitTestMode()) {
      index = myIndex;
    }
    return index;
  }

  public static String getKey(PsiMember c) {
    return c instanceof PsiClass ? ((PsiClass)c).getQualifiedName():c.getContainingClass().getQualifiedName() + "."+ c.getName();
  }

  @NotNull
  public synchronized IndexEntry[] getWsEntries(@NotNull Collection<IndexEntry> entries, PsiMember c) {
    refreshDirtyFiles();
    final String qname = getKey(c);
    final PsiModifierList modifierList = c.getModifierList();
    final String anyqname = modifierList != null ? modifierList.hasModifierProperty(PsiModifier.PUBLIC)? getAnyKey(c):null:null;

    final boolean fileLevel = c.getParent() instanceof PsiFile;
    Set<Module> depedentModules = null;

    final ProjectFileIndex index = ProjectRootManager.getInstance(c.getProject()).getFileIndex();

    if (fileLevel) {
      final Module moduleForFile = index.getModuleForFile(c.getContainingFile().getVirtualFile());

      if (moduleForFile != null) {
        depedentModules = new THashSet<Module>();
        depedentModules.add(moduleForFile);
        final Module[] modules = ModuleRootManager.getInstance(moduleForFile).getDependencies();

        depedentModules.addAll(Arrays.asList(modules));
      }
    }

    List<IndexEntry> result = null;
    for(IndexEntry entry:entries) {
      if (fileLevel) {
        if (depedentModules != null && !depedentModules.contains(index.getModuleForFile(entry.getFile()))) continue;
      }

      result = addEntry(entry, qname, result);

      if (anyqname != null) {
        result = addEntry(entry, anyqname, result);
      }
    }

    return result != null ? result.toArray(new IndexEntry[result.size()]): IndexEntry.EMPTY;
  }

  private static List<IndexEntry> addEntry(IndexEntry entry, String qname, List<IndexEntry> result) {
    if (entry.mySymbols.contains(qname)) {
      Object o = entry.myLinks.get(qname);

      if (o instanceof TextRange) {
        if (result == null) result = new LinkedList<IndexEntry>();
        result.add(entry);
      }
    }
    return result;
  }

  public long getModificationStamp() {
    return myModificationStamp;
  }

  @NotNull
  public synchronized IndexEntry[] getWsEntries(PsiClass c) {
    refreshDirtyFiles();
    if (!myWsClasses.contains(c.getQualifiedName())) {
      return IndexEntry.EMPTY;
    }
    return getWsEntries(myFiles.values(),c);
  }

  private synchronized void refreshDirtyFiles() {
    if (!myDirtyFiles.isEmpty()) {
      for(VirtualFile file:myDirtyFiles) {
        if (!file.isValid()) continue;
        PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
        if (psiFile instanceof XmlFile) myFiles.put(file, new IndexEntry((XmlFile) psiFile));
      }
      myDirtyFiles.clear();
      incrementModificationStamp();
    }
  }

  static String getAnyKey(PsiMember member) {
    final PsiClass aClass = member instanceof PsiMethod ? member.getContainingClass():null;

    if (aClass != null) {
      return aClass.getQualifiedName() + "." + ANY_NAME;
    }
    return null;
  }

  private class TreeChangeListener extends PsiTreeChangeAdapter {
    public void childAdded(PsiTreeChangeEvent event) {
      final PsiElement child = event.getChild();
      if (child instanceof XmlFile && child.isPhysical()) {
        processFileAdded((XmlFile)child);
      }
      else {
        process(event);
      }
    }

    public void childrenChanged(PsiTreeChangeEvent event) {
      process(event);
    }

    public void childRemoved(PsiTreeChangeEvent event) {
      if (event.getChild() instanceof XmlFile) {
        processFileRemoved(((XmlFile)event.getChild()).getVirtualFile());
      }
      else {
        process(event);
      }
    }

    public void childReplaced(PsiTreeChangeEvent event) {
      process(event);
    }

    private void process(final PsiTreeChangeEvent event) {
      PsiFile file = event.getParent().getContainingFile();
      if (event.getParent() != null && file instanceof XmlFile) {
        processFileChanged(file.getVirtualFile());
      }
    }
  }
}
