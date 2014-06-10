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

package com.intellij.uml.model;

import com.intellij.codeInsight.generation.OverrideImplementUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.graph.builder.util.NodeFactory;
import com.intellij.openapi.graph.layout.NodeLayout;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.uml.actions.UmlAction;
import com.intellij.uml.dependency.ClassDependencyAnalizer;
import com.intellij.uml.utils.UmlBundle;
import com.intellij.uml.utils.UmlPsiUtil;
import com.intellij.uml.utils.UmlUtils;
import com.intellij.uml.utils.VcsUtils;
import com.intellij.util.IncorrectOperationException;
import com.intellij.ProjectTopics;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class UmlClassDiagramDataModel extends GraphDataModel<UmlNode, UmlEdge> {
  private final Map<String, SmartPsiElementPointer<PsiClass>> classesAddedByUser = new HashMap<String, SmartPsiElementPointer<PsiClass>>();
  private final Map<String, SmartPsiElementPointer<PsiClass>> classesRemovedByUser = new HashMap<String, SmartPsiElementPointer<PsiClass>>();
  private SmartPsiElementPointer<PsiPackage> initialPackage = null;
  private SmartPsiElementPointer<PsiElement> myInitialElement;
  private final Map<String, SmartPsiElementPointer<PsiPackage>> packages = new HashMap<String, SmartPsiElementPointer<PsiPackage>>();
  private final Map<String, SmartPsiElementPointer<PsiPackage>> packagesRemovedByUser = new HashMap<String, SmartPsiElementPointer<PsiPackage>>();

  private boolean useInnerClasses = false;
  private GraphBuilder<UmlNode, UmlEdge> myBuilder;
  private final VirtualFile myEditorFile;
  private final SmartPointerManager spManager;
  private boolean showDependencies = false;
  private final Project project;

  private boolean myScheduleUpdate = true;

  public UmlClassDiagramDataModel(final PsiElement psiElement, final VirtualFile file) {
    project = psiElement.getProject();
    myEditorFile = file;
    spManager = SmartPointerManager.getInstance(project);
    myInitialElement = spManager.createSmartPsiElementPointer(psiElement);
    if (psiElement instanceof PsiClass) {
      PsiClass psiClass = (PsiClass)psiElement;
      final Set<PsiClass> classes = getAllParentsForClass(psiClass);
      for (PsiClass aClass : classes) {
        classesAddedByUser.put(aClass.getQualifiedName(), spManager.createSmartPsiElementPointer(aClass));
      }
    }
    if (psiElement instanceof PsiPackage) {
      final PsiPackage psiPackage = (PsiPackage)psiElement;
      initialPackage = spManager.createSmartPsiElementPointer(psiPackage);


      for (PsiClass psiClass : psiPackage.getClasses()) {
        classesAddedByUser.put(psiClass.getQualifiedName(), spManager.createSmartPsiElementPointer(psiClass));
        if (useInnerClasses) {
          for (PsiClass aClass : UmlPsiUtil.getAllInnerClasses(psiClass)) {
            classesAddedByUser.put(aClass.getQualifiedName(), spManager.createSmartPsiElementPointer(aClass));
          }
        }
      }

      for (PsiElement element : psiPackage.getSubPackages()) {
        if (element instanceof PsiPackage) {
          final PsiPackage aPackage = (PsiPackage)element;
          final SmartPsiElementPointer<PsiPackage> pointer = spManager.createSmartPsiElementPointer(aPackage);
          packages.put(aPackage.getQualifiedName(), pointer);
        }
      }
    }
    final PsiModificationTracker tracker = PsiManager.getInstance(project).getModificationTracker();
    project.getMessageBus().connect(this).subscribe(ProjectTopics.MODIFICATION_TRACKER, new PsiModificationTracker.Listener() {
      long myStamp = tracker.getJavaStructureModificationCount();
      public void modificationCountChanged() {
        final long stamp = tracker.getJavaStructureModificationCount();
        if (myStamp != stamp) {
          myStamp = stamp;
          scheduleUpdate();
        }
      }
    });
  }

  public UmlClassDiagramDataModel(Project project, VirtualFile file) {
    this.project = project;
    myEditorFile = file;
    spManager = SmartPointerManager.getInstance(project);
    if (VcsUtils.isShowChangesFile(file)) {
      for (PsiClass psiClass : VcsUtils.getChangedClasses(project, VcsUtils.getLocalChangeListFromFile(file, project))) {
        addElement(psiClass, false);
      }
    }
  }

  private final Collection<UmlNode> myNodes = new HashSet<UmlNode>();
  private final Collection<UmlEdge> myEdges = new HashSet<UmlEdge>();
  private final Collection<UmlEdge> myDependencyEdges = new HashSet<UmlEdge>();

  private final Collection<UmlNode> myNodesOld = new HashSet<UmlNode>();
  private final Collection<UmlEdge> myEdgesOld = new HashSet<UmlEdge>();
  private final Collection<UmlEdge> myDependencyEdgesOld = new HashSet<UmlEdge>();


  @NotNull
  public Collection<UmlNode> getNodes() {
    if (myScheduleUpdate) {
      myScheduleUpdate = false;
      refreshDataModel();
    }

    return myNodes;
  }

  @NotNull
  public Collection<UmlEdge> getEdges() {
    if (myDependencyEdges.isEmpty()) {
      return myEdges;
    } else {
      Collection<UmlEdge> allEdges = new HashSet<UmlEdge>(myEdges);
      allEdges.addAll(myDependencyEdges);
      return allEdges;
    }
  }

  public void collapseToPackage(final UmlNode umlNode) {
    scheduleUpdate();
    PsiElement element = umlNode.getIdentifyingElement();
    PsiPackage psiPackage;
    if (element instanceof PsiPackage) {
      psiPackage = ((PsiPackage)element).getParentPackage();
      if (psiPackage == null) return;
    } else {
      psiPackage = UmlUtils.getPackage(element);
    }
    if (psiPackage == null || "".equals(psiPackage.getQualifiedName())) return;
    if (element instanceof PsiPackage) {
      Set<String> packs = new HashSet<String>();
      String search = psiPackage.getQualifiedName() + ".";
      for (String key : packages.keySet()) {
        if (key.startsWith(search)) {
          packs.add(key);
        }
      }

      for (String pack : packs) {
        final SmartPsiElementPointer<PsiPackage> pointer = packages.remove(pack);
        final PsiPackage aPackage;
        if (pointer != null && (aPackage = pointer.getElement()) != null) {
          List<PsiClass> classes = Arrays.asList(aPackage.getClasses());
          Set<String> toRemove = new HashSet<String>();
          for (String key : classesAddedByUser.keySet()) {
            final SmartPsiElementPointer<PsiClass> ptr = classesAddedByUser.get(key);
            PsiClass cls;
            if ((cls = ptr.getElement()) == null || classes.contains(cls)) {
              toRemove.add(key);
            }
          }
          for (String fqn : toRemove) {
            classesAddedByUser.remove(fqn);
          }
          for (PsiClass aClass : classes) {
            for (PsiClass innerClass : aClass.getInnerClasses()) {
              classesAddedByUser.remove(innerClass.getQualifiedName());
            }
          }
        }
      }
    }
    packages.put(psiPackage.getQualifiedName(), spManager.createSmartPsiElementPointer(psiPackage));
    packagesRemovedByUser.remove(psiPackage.getQualifiedName());
  }

  @NotNull
  public UmlNode getSourceNode(final UmlEdge edge) {
    return edge.getSource();
  }

  @NotNull
  public UmlNode getTargetNode(final UmlEdge edge) {
    return edge.getTarget();
  }

  @NotNull
  @NonNls
  public String getNodeName(final UmlNode node) {
    PsiElement element = node.getIdentifyingElement();
    if (element instanceof PsiClass) {
      return "Class " + ((PsiClass)element).getQualifiedName();
    } else if (element instanceof PsiPackage) {
      return "Package " + ((PsiPackage)element).getQualifiedName();
    }
    return "";
  }

  @NotNull
  public String getEdgeName(final UmlEdge edge) {
    return edge.getName();
  }

  public UmlEdge createEdge(@NotNull final UmlNode from, @NotNull final UmlNode to) {
    final String[] errorHolder = new String[1];

    final PsiClass fromClass = (PsiClass)from.getIdentifyingElement();
    final PsiClass toClass = (PsiClass)to.getIdentifyingElement();
    UmlAction.prepareClassForWrite(fromClass);
    UmlPsiUtil.runWriteActionInCommandProcessor(new Runnable() {
      public void run() {
        try {
          if (toClass.isAnnotationType()) {
            errorHolder[0] = UmlPsiUtil.annotateClass(fromClass, toClass);
          } else {
            errorHolder[0] = UmlPsiUtil.createInheritanceBetween(fromClass, toClass);
          }
        } catch (IncorrectOperationException e) {//
        }
      }
    });

    if (errorHolder[0] != null) {
      Messages.showErrorDialog(from.getIdentifyingElement().getProject(),
                               errorHolder[0], UmlBundle.message("error.cant.create.edge"));
      return null;
    } else {
      PsiClass parent = (PsiClass)to.getIdentifyingElement();
      PsiClass child = (PsiClass)from.getIdentifyingElement();
      if (parent == child) return null;

      UmlRelationship relationship;
      if (parent.isInterface()) {
          if (parent.isAnnotationType()) {
            relationship = UmlRelationship.ANNOTATION;
          } else {
            relationship = child.isInterface() ? UmlRelationship.INTERFACE_GENERALIZATION : UmlRelationship.REALIZATION;
          }
      } else {
        relationship = UmlRelationship.GENERALIZATION;
      }
      
      final UmlPsiElementEdge edge = new UmlPsiElementEdge(from, to, relationship);
      myEdges.add(edge);
      if (myBuilder != null) {
        UmlUtils.updateGraph(myBuilder, true, false);
      }

      if (!UmlPsiUtil.isAbstract(child) && !OverrideImplementUtil.getMethodSignaturesToImplement(child).isEmpty()) {
        final Project project = child.getProject();
        int code = Messages.showDialog(project,
                                       UmlBundle.message("class.must.be.abstract.or.implement.methods", child.getName()),
                                       UmlBundle.message("class.must.be.abstract.title"),
                                       new String[]{
                                         UmlBundle.message("generate.methods"),
                                         UmlBundle.message("make.abstract")},
                                       0,
                                       Messages.getQuestionIcon());
        if (code == 0) {
          final PsiFile psiFile = child.getContainingFile();
          final VirtualFile vf;

          if (psiFile == null || (vf = psiFile.getVirtualFile()) == null) return null;

          final Editor editor = FileEditorManager.getInstance(project)
            .openTextEditor(new OpenFileDescriptor(project, vf), false);
          OverrideImplementUtil.chooseAndImplementMethods(project, editor, child);
          FileEditorManager.getInstance(project).openFile(myEditorFile, true);

        } else if (code == 1) {
          UmlPsiUtil.makeClassAbstract(child);
        }
      }
      return edge;
    }
  }

  private void refreshDataModel() {
    clearAll();
    updateDataModel();
  }

  private void clearAll() {    
    clearAndBackup(myNodes,  myNodesOld);
    clearAndBackup(myEdges,  myEdgesOld);
    clearAndBackup(myDependencyEdges,  myDependencyEdgesOld);
  }

  public void removeAllElements() {
    classesRemovedByUser.clear();
    classesRemovedByUser.putAll(classesAddedByUser);
    classesAddedByUser.clear();
    packagesRemovedByUser.clear();
    packagesRemovedByUser.putAll(packages);
    packages.clear();    
    clearAll();
  }

  private boolean isAllowedToShow(PsiClass psiClass) {
    if (psiClass == null || !psiClass.isValid()) return false;
    for (SmartPsiElementPointer<PsiClass> pointer : classesRemovedByUser.values()) {
      if (psiClass.equals(pointer.getElement())) return false;
    }
    final PsiElement initialElement = getInitialElement();
    if (initialElement instanceof  PsiClass && equals(psiClass, (PsiClass)initialElement)) return true;
    if (isInsidePackages(psiClass)) return false;
    if (!useInnerClasses && PsiUtil.isInnerClass(psiClass)) return false;
    return true;
  }

  private static boolean equals(PsiClass one, PsiClass another) {
    return one != null && one.isValid()
        && another != null && another.isValid()
        && UmlUtils.isEqual(one.getQualifiedName(), another.getQualifiedName());
  }

  public synchronized void updateDataModel() {
    final Set<PsiClass> classes = getAllClasses();
    syncPackages();
    final Set<String> interfaces = new HashSet<String>();
    final Set<String> annotations = new HashSet<String>();
    for (SmartPsiElementPointer<PsiPackage> ptr : packages.values()) {
      PsiPackage psiPackage;
      if (ptr != null && (psiPackage = ptr.getElement()) != null) {
        myNodes.add(new UmlPsiElementNode(psiPackage));
      }
    }
    for (PsiClass psiClass : classes) {
      if (isAllowedToShow(psiClass)) {
        myNodes.add(new UmlPsiElementNode(psiClass));
      }

      //think twice if you wanna change this if-else
      if (psiClass.isAnnotationType()) {
        annotations.add(psiClass.getQualifiedName());
      } else if (psiClass.isInterface()) {
        interfaces.add(psiClass.getQualifiedName());
      }
    }

    for (PsiClass psiClass : classes) {
      if (isGeneralizationEdgeAllowed(psiClass)) {
        UmlNode source = findNode(psiClass);
        UmlNode target = null;
        PsiClass superClass = psiClass.getSuperClass();
        while (target == null && superClass != null) {
          target = findNode(superClass);
          superClass = superClass.getSuperClass();
        }

        if (source != null && target != null && source != target) {
          addEdge(source, target, UmlRelationship.GENERALIZATION);
        }
      }
      
      for (PsiClass inter : psiClass.getInterfaces()) {
        if (interfaces.contains(inter.getQualifiedName())) {
          UmlNode source = findNode(psiClass);
          UmlNode target = findNode(inter);
          if (source != null && target != null && source != target) {
            addEdge(source, target, psiClass.isInterface() ? UmlRelationship.INTERFACE_GENERALIZATION : UmlRelationship.REALIZATION);
          }
        }
      }
      if (psiClass.isInterface()) {
        Set<PsiClass> found = new HashSet<PsiClass>();
        findNearestInterfaces(psiClass, interfaces, found);
        for (PsiClass inter : found) {
          UmlNode source = findNode(psiClass);
          UmlNode target = findNode(inter);
          if (source != null && target != null && source != target) {
            addEdge(source, target, UmlRelationship.INTERFACE_GENERALIZATION);
          }
        }
      } else {
        //Collect all realized interfaces
        Set<PsiClass> inters = new HashSet<PsiClass>();
        inters.addAll(Arrays.asList(psiClass.getInterfaces()));
        PsiClass cur = psiClass.getSuperClass();
        while (cur != null) {
          if (findNode(cur) == null) {
            inters.addAll(Arrays.asList(cur.getInterfaces()));
          } else break;
          cur = cur.getSuperClass();
        }

        ArrayList<PsiClass> faces = new ArrayList<PsiClass>(inters);

        while (! faces.isEmpty()) {
          PsiClass inter = faces.get(0);
          if (findNode(inter) != null) {
            UmlNode source = findNode(psiClass);
            UmlNode target = findNode(inter);
            if (source != null && target != null && source != target) {
              addEdge(source, target, UmlRelationship.REALIZATION);
            }
            faces.remove(inter);
          } else {
            faces.remove(inter);
            faces.addAll(Arrays.asList(inter.getInterfaces()));
          }
        }
      }

      if (!isInsidePackages(psiClass) && useInnerClasses) {
        for (PsiClass inner : psiClass.getInnerClasses()) {
          if (classes.contains(inner)) {
            UmlNode source = findNode(inner);
            UmlNode target = findNode(psiClass);
            if (source != null && target != null && source != target) {
              addEdge(source, target, UmlRelationship.INNER_CLASS);
            }
          }
        }
      }

      //annotations
      for (PsiClass annotation : UmlPsiUtil.findAnnotationsForClass(psiClass)) {
        if (annotations.contains(annotation.getQualifiedName())) {
          UmlNode source = findNode(psiClass);
          UmlNode target = findNode(annotation);
          if (source != null && target != null && source != target) {
            addEdge(source, target, UmlRelationship.ANNOTATION);
          }
        }
      }

      if (showDependencies) {
        showDependenciesFor(psiClass);
      }
    }

    //merge!
    mergeWithBackup(myNodes, myNodesOld);
    mergeWithBackup(myEdges,  myEdgesOld);
    mergeWithBackup(myDependencyEdges,  myDependencyEdgesOld);
  }

  private static <T> void clearAndBackup(Collection<T> target, Collection<T> backup) {
    backup.clear();
    backup.addAll(target);
    target.clear();
  }

  private static <T> void mergeWithBackup(Collection<T> target, Collection<T> backup) {
    for (T t : backup) {
      if (target.contains(t)) {
        target.remove(t);
        target.add(t);
      }
    }
  }

  private void syncPackages() {
    if (initialPackage == null) return;
    final PsiPackage initPackage = initialPackage.getElement();
    if (initPackage == null) return;

    final Map<String, PsiPackage> psiPackages = new HashMap<String, PsiPackage>();
    for (PsiPackage sub : initPackage.getSubPackages()) {
      psiPackages.put(sub.getQualifiedName(), sub);
    }
    for (String fqn : packages.keySet()) psiPackages.remove(fqn);
    for (String fqn : packagesRemovedByUser.keySet()) psiPackages.remove(fqn);

    if (psiPackages.size() > 0) {
      for (PsiPackage psiPackage : psiPackages.values()) {
        packages.put(psiPackage.getQualifiedName(), spManager.createSmartPsiElementPointer(psiPackage));
      }
    }
  }

  private static void findNearestInterfaces(final PsiClass psiClass, final Set<String> interfaces, final Set<PsiClass> found) {
    for (PsiClass inter : psiClass.getInterfaces()) {
      if (interfaces.contains(inter.getQualifiedName())) {
        found.add(inter);
      } else {
        findNearestInterfaces(inter, interfaces, found);
      }
    }
  }

  private static boolean isGeneralizationEdgeAllowed(final PsiClass psiClass) {
    return !psiClass.isInterface()
           && !psiClass.isAnnotationType();
  }

  private boolean isInsidePackages(PsiClass psiClass) {
    return packages.get(UmlUtils.getRealPackageName(psiClass)) != null;
  }

  public void addEdge(UmlNode from, UmlNode to, UmlRelationship relationship) {
    addEdge(from, to, relationship, myEdges);
  }

  public void addDependencyEdge(UmlNode from, UmlNode to, UmlRelationship relationship) {
    addEdge(from, to, relationship, myDependencyEdges);
  }

  private static void addEdge(UmlNode from, UmlNode to, UmlRelationship relationship, Collection<UmlEdge> storage) {
    for (UmlEdge edge : storage) {
      if (edge.getSource() == from
          && edge.getTarget() == to
          && edge.getArrow() == relationship.getArrow()
          && edge.getLineType() == relationship.getLineType()) return;
    }
    storage.add(new UmlPsiElementEdge(from, to, relationship));
  }

  private Set<PsiClass> getAllClasses() {
    Set<PsiClass> classes = new HashSet<PsiClass>();
    for (SmartPsiElementPointer<PsiClass> pointer : classesAddedByUser.values()) {
      classes.add(pointer.getElement());
    }
    if (initialPackage != null) {
      final PsiPackage initPackage = initialPackage.getElement();
      if (initPackage != null) {
        classes.addAll(Arrays.asList(initPackage.getClasses()));
      }
    }
    for (SmartPsiElementPointer<PsiPackage> ptr : packages.values()) {
      PsiPackage psiPackage;
      if (ptr != null && (psiPackage = ptr.getElement()) != null) {
        classes.addAll(Arrays.asList(psiPackage.getClasses()));
      }
    }
    classes.remove(null);
    Set<PsiClass> temp = new HashSet<PsiClass>();
    if (useInnerClasses) {
      for (PsiClass aClass : classes) {
        temp.addAll(UmlPsiUtil.getAllInnerClasses(aClass));
      }
      classes.addAll(temp);
      temp.clear();
    }

    classes.remove(null);
    for (PsiClass aClass : classes) {
      if (! aClass.isValid()) temp.add(aClass);
    }

    for (SmartPsiElementPointer<PsiClass> cls : classesRemovedByUser.values()) {
      classes.remove(cls.getElement());
    }
    classes.removeAll(temp);
    return classes;
  }

  private static Set<PsiClass> getAllParentsForClass(PsiClass cl) {
    return findAllParentsForClass(cl, new HashSet<PsiClass>());
  }

  private static Set<PsiClass> findAllParentsForClass(@NotNull PsiClass clazz, @NotNull Set<PsiClass> found) {
    found.add(clazz);
    for (PsiClass psiClass : clazz.getSupers()) {
      if (psiClass.getSuperClass() != null) {
        found.add(psiClass);
        found.addAll(findAllParentsForClass(psiClass, found));
      } else if (! clazz.isInterface()){
        found.add(psiClass);
      }
    }
    final PsiModifierList modifierList = clazz.getModifierList();
    if (modifierList != null) {
      final PsiAnnotation[] annotations = modifierList.getAnnotations();
      for (PsiAnnotation annotation : annotations) {
        final PsiClass anno = UmlPsiUtil.findAnnotationClass(annotation);
        if (anno != null) found.add(anno);
      }
    }
    return found;
  }

  @Nullable
  public UmlNode findNode(PsiElement psiElement) {
    for (UmlNode node : myNodes) {
      final String fqn = UmlUtils.getFQN(node.getIdentifyingElement());
      if (fqn != null && fqn.equals(UmlUtils.getFQN(psiElement))) {
        return node;
      }
    }
    final SmartPsiElementPointer<PsiPackage> ptr = packages.get(UmlUtils.getPackageName(psiElement));
    return  ptr == null ? null : findNode(ptr.getElement());
  }

  public boolean contains(PsiElement psiElement) {
    return findNode(psiElement) != null;
  }

  public void dispose() {
  }

  public void removeElement(final PsiElement element) {
    scheduleUpdate();
    UmlNode node = findNode(element);
    if (node == null) {
      classesAddedByUser.remove(UmlUtils.getFQN(element));
      return;
    }

    Collection<UmlEdge> edges = new ArrayList<UmlEdge>();
    for (UmlEdge edge : myEdges) {
      if (edge.getTarget() == node || edge.getSource() == node) {
        edges.add(edge);
      }
    }
    myEdges.removeAll(edges);
    myNodes.remove(node);
    if (element instanceof PsiClass) {
      final PsiClass psiClass = (PsiClass)element;
      classesRemovedByUser.put(psiClass.getQualifiedName(), spManager.createSmartPsiElementPointer(psiClass));
      classesAddedByUser.remove(psiClass.getQualifiedName());
      for (PsiClass innerClass : psiClass.getInnerClasses()) {
        classesRemovedByUser.put(innerClass.getQualifiedName(), spManager.createSmartPsiElementPointer(innerClass));
        classesAddedByUser.remove(innerClass.getQualifiedName());
      }      
    }
    if (element instanceof PsiPackage) {
      PsiPackage p = (PsiPackage)element;
      packages.remove(p.getQualifiedName());
      packagesRemovedByUser.put(p.getQualifiedName(), spManager.createSmartPsiElementPointer(p));

      Set<String> toDelete = new HashSet<String>();
      for (String key : classesAddedByUser.keySet()) {
        final SmartPsiElementPointer<PsiClass> pointer = classesAddedByUser.get(key);
        final PsiClass psiClass = pointer.getElement();
        if (UmlUtils.isEqual(p.getQualifiedName(), UmlUtils.getRealPackageName(psiClass))) {
          toDelete.add(key);
        }
      }
      for (String key : toDelete) {
        classesAddedByUser.remove(key);
      }
    }
  }

  private void scheduleUpdate() {
    myScheduleUpdate = true;
  }

  @Nullable
  public UmlNode addElement(PsiElement element) {
    return addElement(element, true);
  }

  @Nullable
  public UmlNode addElement(final PsiElement element, boolean createNodeInBuilder) {
    if (findNode(element) != null) return null;
    scheduleUpdate();

    if (element instanceof PsiPackage) {
      PsiPackage psiPackage = (PsiPackage)element;
      final String fqn = psiPackage.getQualifiedName();
      if (fqn.length() == 0) return null;
      packages.put(fqn, spManager.createSmartPsiElementPointer(psiPackage));
      packagesRemovedByUser.remove(fqn);
    } else if (element instanceof PsiClass) {
      PsiClass psiClass = (PsiClass)element;
      if (psiClass.getQualifiedName() == null || isInsidePackages(psiClass)) return null;
      final SmartPsiElementPointer<PsiClass> pointer = spManager.createSmartPsiElementPointer(psiClass);
      final String fqn = psiClass.getQualifiedName();
      classesAddedByUser.put(fqn, pointer);
      classesRemovedByUser.remove(fqn);
    } else {
      return null;
    }
    final UmlPsiElementNode node = new UmlPsiElementNode(element);
    if (createNodeInBuilder) {
      final Point point = UmlUtils.getBestPositionForNode(myBuilder);
      NodeFactory.getInstance().createDraggedNode(myBuilder, node, getNodeName(node), point);
      myNodes.add(node);
      final Node nodeObj = myBuilder.getNode(node);
      if (nodeObj != null) {
        final NodeLayout nodeLayout = myBuilder.getGraph().getNodeLayout(nodeObj);
        if (nodeLayout != null) {
          nodeLayout.setLocation(point.x, point.y);
        }
      }
      //UmlUtils.updateGraph(myBuilder, true, false);
    }
    return node;
  }

  public void expandPackage(final PsiPackage psiPackage) {
    scheduleUpdate();
    packages.remove(psiPackage.getQualifiedName());
    packagesRemovedByUser.put(psiPackage.getQualifiedName(), spManager.createSmartPsiElementPointer(psiPackage));
    for (PsiClass psiClass : psiPackage.getClasses()) {
      addElement(psiClass, false);
      for (PsiClass inner : psiClass.getInnerClasses()) {
        addElement(inner, false);
      }
    }
    for (PsiPackage aPackage : psiPackage.getSubPackages()) {
      addElement(aPackage, false);
    }
  }

  public void showDependencies(boolean show) {
    showDependencies = show;
  }

  private void showDependenciesFor(PsiClass psiClass) {
    UmlNode mainNode = findNode(psiClass);

    if (mainNode == null) return;

    ClassDependencyAnalizer analizer = new ClassDependencyAnalizer(psiClass);

    List<Pair<PsiClass, UmlRelationship>> list = analizer.computeUsedClasses();
    for (Pair<PsiClass, UmlRelationship> pair : list) {
      UmlNode node = findNode(pair.first);
      if (node != null) {
        addDependencyEdge(mainNode, node, pair.second);
      }
    }

    list = analizer.computeUsingClasses();
    for (Pair<PsiClass, UmlRelationship> pair : list) {
      UmlNode node = findNode(pair.first);
      if (node != null) {
        addDependencyEdge(node, mainNode, pair.second);
      }
    }
  }

  public void setUseInnerClasses(boolean use) {
    useInnerClasses = use;
  }

  List<String> getAllClassesFQN() {
    List<String> fqns = new ArrayList<String>();
    for (UmlNode node : myNodes) {
      if (node.getIdentifyingElement() instanceof PsiClass) {
        fqns.add(((PsiClass)node.getIdentifyingElement()).getQualifiedName());
      }
    }
    return fqns;
  }

  List<String> getAllPackagesFQN() {
    List<String> fqns = new ArrayList<String>();
    for (UmlNode node : myNodes) {
      if (node.getIdentifyingElement() instanceof PsiPackage) {
        fqns.add(((PsiPackage)node.getIdentifyingElement()).getQualifiedName());
      }
    }
    return fqns;
  }

  @Nullable
  public PsiElement getInitialElement() {
    if (myInitialElement == null) return null;
    final PsiElement element = myInitialElement.getElement();
    return element == null || !element.isValid() ? null : element;
  }

  public void setBuilder(GraphBuilder<UmlNode, UmlEdge> builder) {
    myBuilder = builder;
  }

  public boolean hasNotValid() {
    for (UmlNode node : myNodes) {
      if (! node.getIdentifyingElement().isValid()) {
        return true;
      }
    }
    return false;
  }

  public VirtualFile getFile() {
    return myEditorFile;
  }
}
