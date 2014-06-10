package com.intellij.seam.graph;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.graph.builder.NodesGroup;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.PagesFileDomFileDescription;
import com.intellij.seam.graph.beans.*;
import com.intellij.seam.model.xml.PagesDomModelManager;
import com.intellij.seam.model.xml.PagesModel;
import com.intellij.seam.model.xml.pages.*;
import com.intellij.util.Function;
import com.intellij.util.containers.HashSet;
import com.intellij.util.xml.*;
import com.intellij.javaee.web.WebUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: Sergey.Vasiliev
 */
public class PagesDataModel extends GraphDataModel<BasicPagesNode, BasicPagesEdge> {
  private final Collection<BasicPagesNode> myNodes = new HashSet<BasicPagesNode>();
  private final Collection<BasicPagesEdge> myEdges = new HashSet<BasicPagesEdge>();
  protected final Map<PsiFile, NodesGroup> myGroups = new HashMap<PsiFile, NodesGroup>();

  private final Project myProject;
  private final XmlFile myFile;
  @Nullable private final Module myModule;

  @NonNls private static final String UNDEFINED_NAME = "Undefined";
  @NonNls private static final String DEFAULT_EXCEPTION = "Exception";


  public PagesDataModel(final XmlFile file) {
    myFile = file;
    myProject = file.getProject();
    myModule = ModuleUtil.findModuleForPsiElement(file);
  }

  public Project getProject() {
    return myProject;
  }

  @NotNull
  public Collection<BasicPagesNode> getNodes() {
    return getNodes(true);
  }

  @NotNull
  public Collection<BasicPagesNode> getNodes(boolean refresh) {
    if (refresh) refreshDataModel();

    return myNodes;
  }

  @NotNull
  public Collection<BasicPagesEdge> getEdges() {
    return myEdges;
  }

  @NotNull
  public BasicPagesNode getSourceNode(final BasicPagesEdge pagesEdge) {
    return pagesEdge.getSource();
  }

  @NotNull
  public BasicPagesNode getTargetNode(final BasicPagesEdge pagesEdge) {
    return pagesEdge.getTarget();
  }

  @NotNull
  public String getNodeName(final BasicPagesNode pagesNode) {
    return "";
  }

  @NotNull
  public String getEdgeName(final BasicPagesEdge pagesEdge) {
    return pagesEdge.getName();
  }

  public BasicPagesEdge createEdge(@NotNull final BasicPagesNode from, @NotNull final BasicPagesNode to) {
    if (StringUtil.isEmptyOrSpaces(to.getName())) return null;
    if (from instanceof PageNode) {
      final PageNode pageNode = (PageNode)from;
      final Page page = pageNode.getIdentifyingElement();
      if (page.isValid()) {
        final WriteCommandAction<NavigationEdge> action = new WriteCommandAction<NavigationEdge>(myProject) {
          protected void run(final Result<NavigationEdge> result) throws Throwable {
            final Navigation navigation = page.addNavigation().createStableCopy();
            final Redirect redirect = navigation.getRedirect().createStableCopy();
            redirect.getViewId().setStringValue(to.getName());

            result.setResult(new NavigationEdge(from, to, redirect, navigation));
          }
        };
        return action.execute().getResultObject();
      }
    }
    else if (from instanceof ExceptionNode) {
      final ExceptionNode exceptionNode = (ExceptionNode)from;
      final PagesException exception = exceptionNode.getIdentifyingElement();
      if (exception.isValid()) {
        final WriteCommandAction<ExceptionEdge> action = new WriteCommandAction<ExceptionEdge>(myProject) {
          protected void run(final Result<ExceptionEdge> result) throws Throwable {
            final Redirect redirect = exception.getRedirect().createStableCopy();
            redirect.getViewId().setStringValue(to.getName());

            result.setResult(new ExceptionEdge(from, to, redirect, exception));
          }
        };
        return action.execute().getResultObject();
      }
    }

    return null;
  }

  public void dispose() {
  }


  private void refreshDataModel() {
    clearAll();
    updateDataModel();
  }

  private void clearAll() {
    myNodes.clear();
    myEdges.clear();
  }

  public void updateDataModel() {
    PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    
    final Pages pages = getPages();

    if (pages == null) return;

    Map<String, List<PageNode>> allPageNodes = new HashMap<String, List<PageNode>>();
    // collect pages
    collectPageNodes(pages.getPages(), allPageNodes);
    collectPageNodes(getPagesFromFiles(), allPageNodes);

    Map<String, List<ExceptionNode>> allExceptionNodes = getExceptionNodes(pages);
    Map<String, UndefinedPageNode> undefinedPageNodes = new HashMap<String, UndefinedPageNode>();
    final Ref<PagesViewIdOwner> ref = new Ref<PagesViewIdOwner>();
    for (List<PageNode> pageNodes : allPageNodes.values()) {
      for (PageNode sourceNode : pageNodes) {
        final Page page = sourceNode.getIdentifyingElement();
        for (Navigation navigation : page.getNavigations()) {
          addTransitions(sourceNode, navigation, allPageNodes, undefinedPageNodes,
                         getNavigationEdgeCreateFunction(sourceNode, ref, navigation), ref);
          for (Rule rule : navigation.getRules()) {
            addTransitions(sourceNode, rule, allPageNodes, undefinedPageNodes, getRuleEdgeCreateFunction(sourceNode, ref, rule), ref);
          }
        }
      }
    }

    for (List<ExceptionNode> exceptionNodes : allExceptionNodes.values()) {
      for (ExceptionNode exceptionNode : exceptionNodes) {
        final PagesException pagesException = exceptionNode.getIdentifyingElement();
        addExceptionRedirectTransitions(exceptionNode, allPageNodes, pagesException, pagesException.getRedirect(), undefinedPageNodes);
      }
    }
  }

  private static Function<BasicPagesNode, BasicPagesEdge> getNavigationEdgeCreateFunction(final PageNode sourceNode,
                                                                                          final Ref<PagesViewIdOwner> ref,
                                                                                          final Navigation navigation) {
    return new Function<BasicPagesNode, BasicPagesEdge>() {
      public BasicPagesEdge fun(final BasicPagesNode targetNode) {
        return new NavigationEdge(sourceNode, targetNode, ref.get(), (Navigation)navigation.createStableCopy());
      }
    };
  }

  private static Function<BasicPagesNode, BasicPagesEdge> getRuleEdgeCreateFunction(final PageNode sourceNode,
                                                                                    final Ref<PagesViewIdOwner> ref,
                                                                                    final Rule rule) {
    return new Function<BasicPagesNode, BasicPagesEdge>() {
      public BasicPagesEdge fun(final BasicPagesNode targetNode) {
        return new RuleEdge(sourceNode, targetNode, ref.get(), (Rule)rule.createStableCopy());
      }
    };
  }

  private void addTransitions(final PageNode sourceNode,
                              final RenderAndRedirectOwner go,
                              final Map<String, List<PageNode>> allPageNodes,
                              final Map<String, UndefinedPageNode> undefinedPageNodes,
                              final Function<BasicPagesNode, BasicPagesEdge> function,
                              @NotNull final Ref<PagesViewIdOwner> ref) {
    // add redirect
    ref.set(go.getRedirect());
    addViewIdTransitions(allPageNodes, ref.get(), undefinedPageNodes, function);

    // add render
    ref.set(go.getRender());
    addViewIdTransitions(allPageNodes, ref.get(), undefinedPageNodes, function);
  }


  private void addExceptionRedirectTransitions(final ExceptionNode exceptionNode,
                                               final Map<String, List<PageNode>> allPageNodes,
                                               final PagesException pagesException,
                                               final Redirect redirect,
                                               final Map<String, UndefinedPageNode> undefinedPageNodes) {
    addViewIdTransitions(allPageNodes, redirect, undefinedPageNodes, new Function<BasicPagesNode, BasicPagesEdge>() {
      public BasicPagesEdge fun(final BasicPagesNode targetNode) {
        return new ExceptionEdge(exceptionNode, targetNode, redirect, (PagesException)pagesException.createStableCopy());
      }
    });

  }

  private void addViewIdTransitions(final Map<String, List<PageNode>> allPageNodes,
                                    final PagesViewIdOwner viewId,
                                    final Map<String, UndefinedPageNode> undefinedPageNodes,
                                    Function<BasicPagesNode, BasicPagesEdge> createEdgeFunction) {
    final String toViewID = viewId.getViewId().getStringValue();
    if (toViewID != null) {
      List<? extends BasicPagesNode> targetNodes = getTargetNodes(toViewID, viewId, allPageNodes, undefinedPageNodes);
      for (BasicPagesNode targetNode : targetNodes) {
        addTransition(createEdgeFunction.fun(targetNode));
      }
    }
  }

  private List<? extends BasicPagesNode> getTargetNodes(final String redirectTo,
                                                        final PagesViewIdOwner viewId,
                                                        final Map<String, List<PageNode>> allPageNodes,
                                                        final Map<String, UndefinedPageNode> undefinedPageNodes) {
    final List<PageNode> pagesNodeList = allPageNodes.get(redirectTo);
    if (pagesNodeList != null) return pagesNodeList;

    return Collections.singletonList(getOrCreateUndefinedPageNode(redirectTo, viewId, undefinedPageNodes));
  }

  @NotNull
  private UndefinedPageNode getOrCreateUndefinedPageNode(final String redirectTo,
                                                         final PagesViewIdOwner viewId,
                                                         final Map<String, UndefinedPageNode> undefinedPageNodes) {
    final UndefinedPageNode undefinedPageNode = undefinedPageNodes.get(redirectTo);
    if (undefinedPageNode != null) return undefinedPageNode;

    final UndefinedPageNode node = new UndefinedPageNode((PagesViewIdOwner)viewId.createStableCopy(), redirectTo);

    addNode(node);
    undefinedPageNodes.put(redirectTo, node);

    return node;
  }

  private void addNode(final BasicPagesNode node) {
    myNodes.add(node);
  }

  private void addTransition(final BasicPagesEdge edge) {
    myEdges.add(edge);
  }

  private void collectPageNodes(@NotNull final List<Page> pages, @NotNull final Map<String, List<PageNode>> pageNodes) {
    for (Page page : pages) {
      if (!DomUtil.hasXml(page)) continue;

      String name = getPageName(page);
      if (!pageNodes.containsKey(name)) {
        pageNodes.put(name, new ArrayList<PageNode>());
      }
      final PageNode pageNode = new PageNode((Page)page.createStableCopy(), name);
      pageNodes.get(name).add(pageNode);

      addNode(pageNode);
    }
  }

  private static String getPageName(final Page page) {
    String name = page.getViewId().getStringValue();

    if (!StringUtil.isEmptyOrSpaces(name)) return name;

    if (DomUtil.getFileElement(page).getFileDescription().getClass().getName().equals(PagesFileDomFileDescription.class.getName())) {
      final XmlFile xmlFile = DomUtil.getFile(page);
      final String s = xmlFile.getName();
      @NonNls final String suffix = ".page.xml";
      if (s.contains(suffix)) {
        name = s.replaceAll(suffix, "");

        if (!StringUtil.isEmptyOrSpaces(name)) {
          final PsiDirectory psiDirectory = xmlFile.getParent();

          if (psiDirectory != null) {
            for (PsiFile file : psiDirectory.getFiles()) {
              if (file.equals(xmlFile)) continue;
              final String fileName = file.getName();
              if (fileName.startsWith(name)) {
                final String webPath = WebUtil.getWebUtil().getWebPath(file);
                if (webPath != null) {
                  return webPath;
                }
              }
            }
          }
        }
        return s;
      }
    }
    return UNDEFINED_NAME;
  }

  private Map<String, List<ExceptionNode>> getExceptionNodes(final Pages pages) {
    Map<String, List<ExceptionNode>> exceptionNodes = new HashMap<String, List<ExceptionNode>>();
    for (PagesException exception : pages.getExceptions()) {
      String className = exception.getClazz().getStringValue();

      if (StringUtil.isEmptyOrSpaces(className)) className = DEFAULT_EXCEPTION;
      if (!exceptionNodes.containsKey(className)) {
        exceptionNodes.put(className, new ArrayList<ExceptionNode>());
      }
      final ExceptionNode pageNode = new ExceptionNode((PagesException)exception.createStableCopy(), className);
      exceptionNodes.get(className).add(pageNode);

      addNode(pageNode);
    }

    return exceptionNodes;
  }

  @Nullable
  public Pages getPages() {
    final PagesModel model = getModel();
    if (model == null || model.getRoots().size() != 1) return null;

    return model.getRoots().get(0).getRootElement();
  }

  @Nullable
  public PagesModel getModel() {
    return PagesDomModelManager.getInstance(myProject).getPagesModel(myFile);
  }


  private List<Page> getPagesFromFiles() {
    List<Page> models = new ArrayList<Page>();

    if (myModule != null) {
      final GlobalSearchScope moduleContentScope = new ModuleContentRootSearchScope(myModule);
      final Collection<VirtualFile> files =
        DomService.getInstance().getDomFileCandidates(Page.class, myModule.getProject(), moduleContentScope);

      for (VirtualFile pageflowlFile : files) {
        final PsiFile file = PsiManager.getInstance(myModule.getProject()).findFile(pageflowlFile);
        if (file instanceof XmlFile) {

          final DomFileElement<Page> fileElement = DomManager.getDomManager(file.getProject()).getFileElement((XmlFile)file, Page.class);
          if (fileElement != null && fileElement.isValid()) {
            final Page page = fileElement.getRootElement();
            if (page.isValid()) {
              models.add(page);
            }
          }

        }
      }
    }

    return models;
  }
}
