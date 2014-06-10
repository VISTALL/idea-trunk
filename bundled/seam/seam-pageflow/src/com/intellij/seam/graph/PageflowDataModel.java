package com.intellij.seam.graph;

import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.graph.builder.NodesGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.application.Result;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.seam.graph.impl.*;
import com.intellij.seam.model.xml.PageflowDomModelManager;
import com.intellij.seam.model.xml.PageflowModel;
import com.intellij.seam.model.xml.pageflow.*;
import com.intellij.util.containers.HashSet;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PageflowDataModel extends GraphDataModel<PageflowNode, PageflowEdge> {
  private final Collection<PageflowNode> myNodes = new HashSet<PageflowNode>();
  private final Collection<PageflowEdge> myEdges = new HashSet<PageflowEdge>();
  protected final Map<PsiFile, NodesGroup> myGroups = new HashMap<PsiFile, NodesGroup>();

  private final Project myProject;
  private final XmlFile myFile;
  @NonNls private static final String UNDEFINED_NAME = "Undefined";

  public PageflowDataModel(final XmlFile file) {
    myFile = file;
    myProject = file.getProject();
  }

  public Project getProject() {
    return myProject;
  }

  @NotNull
  public Collection<PageflowNode> getNodes() {
    return getNodes(true);
  }

  @NotNull
  public Collection<PageflowNode> getNodes(boolean refresh) {
    if (refresh) refreshDataModel();

    return myNodes;
  }

  @NotNull
  public Collection<PageflowEdge> getEdges() {
    return myEdges;
  }

  @NotNull
  public PageflowNode getSourceNode(final PageflowEdge pageflowBasicEdge) {
    return pageflowBasicEdge.getSource();
  }

  @NotNull
  public PageflowNode getTargetNode(final PageflowEdge pageflowBasicEdge) {
    return pageflowBasicEdge.getTarget();
  }

  @NotNull
  public String getNodeName(final PageflowNode pageflowBasicNode) {
    return "";//pageflowBasicNode.getName();
  }

  @NotNull
  public String getEdgeName(final PageflowEdge pageflowBasicEdge) {
    return pageflowBasicEdge.getName();
  }

  public PageflowEdge createEdge(@NotNull final PageflowNode from, @NotNull final PageflowNode to) {
    final String toName = to.getName();
    final DomElement element = from.getIdentifyingElement();
    if (element instanceof PageflowTransitionHolder) {
      final WriteCommandAction<PageflowEdge> action = new WriteCommandAction<PageflowEdge>(myProject) {
        protected void run(final Result<PageflowEdge> result) throws Throwable {
          final Transition transition = ((PageflowTransitionHolder)element).addTransition();
          transition.getTo().setStringValue(toName);
          result.setResult(new PageflowBasicEdge(from, to, (Transition)transition.createStableCopy(), false));
        }
      };
      return action.execute().getResultObject();
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
    final PageflowDefinition pageflowDefinition = getPageflowDefinition();

    if (pageflowDefinition == null) return;

    Map<String, List<PageNode>> allPageNodes = getPageNodes(pageflowDefinition);
    Map<String, List<DecisionNode>> allDecisionNodes = getDecisionNodes(pageflowDefinition);
    Map<String, List<EndStateNode>> allEndStates = getEndStateNodes(pageflowDefinition);
    Map<String, List<ProcessStateNode>> allProcessStates = getProcessStateNodes(pageflowDefinition);

    final StartState startState = pageflowDefinition.getStartState();
    if (DomUtil.hasXml(startState)) {
      StartStateNode startStateNode = new StartStateNode(startState.getName().getStringValue(), (StartState)startState.createStableCopy());
      addNode(startStateNode);

      addTransitions(startStateNode, startState.getTransitions(), allPageNodes, allDecisionNodes, allEndStates, allProcessStates);
    }

    for (List<PageNode> pageNodes : allPageNodes.values()) {
      for (PageNode pageNode : pageNodes) {
        addTransitions(pageNode, pageNode.getIdentifyingElement().getTransitions(), allPageNodes, allDecisionNodes,  allEndStates, allProcessStates);
      }
    }

    for (List<DecisionNode> decisionNodes : allDecisionNodes.values()) {
      for (DecisionNode decisionNode : decisionNodes) {
        addTransitions(decisionNode, decisionNode.getIdentifyingElement().getTransitions(), allPageNodes, allDecisionNodes, allEndStates,
                       allProcessStates);
      }
    }

    for (List<ProcessStateNode> processStateNodes : allProcessStates.values()) {
      for (ProcessStateNode processStateNode : processStateNodes) {
        addTransitions(processStateNode, processStateNode.getIdentifyingElement().getTransitions(), allPageNodes, allDecisionNodes, allEndStates,
                       allProcessStates);
      }
    }
  }

  private Map<String, List<ProcessStateNode>> getProcessStateNodes(final PageflowDefinition pageflowDefinition) {
    Map<String, List<ProcessStateNode>> processStateNodes = new HashMap<String, List<ProcessStateNode>>();

    for (ProcessState processState : pageflowDefinition.getProcessStates()) {
      String name = processState.getName().getStringValue();

      if (StringUtil.isEmptyOrSpaces(name)) name = UNDEFINED_NAME;
      if (!processStateNodes.containsKey(name)) {
        processStateNodes.put(name, new ArrayList<ProcessStateNode>());
      }
      final ProcessStateNode node = new ProcessStateNode(name, (ProcessState)processState.createStableCopy());

      processStateNodes.get(name).add(node);
      addNode(node);
    }

    return processStateNodes;
  }

  private Map<String, List<EndStateNode>> getEndStateNodes(final PageflowDefinition pageflowDefinition) {
    Map<String, List<EndStateNode>> endStateNodes = new HashMap<String, List<EndStateNode>>();

    for (EndState endState : pageflowDefinition.getEndStates()) {
      String name = endState.getName().getStringValue();

      if (StringUtil.isEmptyOrSpaces(name)) name = UNDEFINED_NAME;
      if (!endStateNodes.containsKey(name)) {
        endStateNodes.put(name, new ArrayList<EndStateNode>());
      }
      final EndStateNode node = new EndStateNode(name, (EndState)endState.createStableCopy());

      endStateNodes.get(name).add(node);
      addNode(node);
    }

    return endStateNodes;
  }

  private void addTransitions(final PageflowNode<? extends DomElement> sourceNode,
                              final List<Transition> transitions,
                              final Map<String, List<PageNode>> allPageNodes,
                              final Map<String, List<DecisionNode>> allDecisionNodes,
                              final Map<String, List<EndStateNode>> allEndStates,
                              final Map<String, List<ProcessStateNode>> allProcessStates) {
    for (Transition transition : transitions) {
      final String targetNodeName = transition.getTo().getStringValue();
      if (StringUtil.isEmptyOrSpaces(targetNodeName)) continue;

      final List<PageNode> targetPageNodes = allPageNodes.get(targetNodeName);
      if (targetPageNodes != null) {
        for (PageNode targetNode : targetPageNodes) {
          addTransition(new PageflowBasicEdge(sourceNode, targetNode, (Transition)transition.createStableCopy(), (targetPageNodes.size() > 1 ||
                                                                                   allDecisionNodes.containsKey(targetNodeName) ||
                                                                                   allEndStates.containsKey(targetNodeName) ||
                                                                                   allProcessStates.containsKey(targetNodeName) )));
        }
      }

      final List<DecisionNode> targetDecisionNodes = allDecisionNodes.get(targetNodeName);
      if (targetDecisionNodes != null) {
        for (DecisionNode targetNode : targetDecisionNodes) {
          addTransition(new PageflowBasicEdge(sourceNode, targetNode, (Transition)transition.createStableCopy(), (targetDecisionNodes.size() > 1 ||
                                                                                   allPageNodes.containsKey(targetNodeName) ||
                                                                                   allEndStates.containsKey(targetNodeName)||
                                                                                   allProcessStates.containsKey(targetNodeName))));
        }
      }

      final List<EndStateNode> targetEndStateNodes = allEndStates.get(targetNodeName);
      if (targetEndStateNodes != null) {
        for (EndStateNode targetNode : targetEndStateNodes) {
          addTransition(new PageflowBasicEdge(sourceNode, targetNode, (Transition)transition.createStableCopy(), (targetEndStateNodes.size() > 1 ||
                                                                                   allPageNodes.containsKey(targetNodeName) ||
                                                                                   allDecisionNodes.containsKey(targetNodeName)||
                                                                                   allProcessStates.containsKey(targetNodeName))));
        }
      }

      final List<ProcessStateNode> targetProcessStateNodes = allProcessStates.get(targetNodeName);
      if (targetProcessStateNodes != null) {
        for (ProcessStateNode targetNode : targetProcessStateNodes) {
          addTransition(new PageflowBasicEdge(sourceNode, targetNode, (Transition)transition.createStableCopy(), (targetProcessStateNodes.size() > 1 ||
                                                                                   allPageNodes.containsKey(targetNodeName) ||
                                                                                   allDecisionNodes.containsKey(targetNodeName)||
                                                                                   allEndStates.containsKey(targetNodeName))));
        }
      }
    }
  }

  private void addNode(final PageflowNode node) {
    myNodes.add(node);
  }

  private void addTransition(final PageflowEdge edge) {
    myEdges.add(edge);
  }

  private Map<String, List<DecisionNode>> getDecisionNodes(final PageflowDefinition pageflowDefinition) {
    Map<String, List<DecisionNode>> decisions = new HashMap<String, List<DecisionNode>>();

    for (Decision decision : pageflowDefinition.getDecisions()) {
      String name = decision.getName().getStringValue();

      if (StringUtil.isEmptyOrSpaces(name)) name = UNDEFINED_NAME;
      if (!decisions.containsKey(name)) {
        decisions.put(name, new ArrayList<DecisionNode>());
      }
      final DecisionNode decisionNode = new DecisionNode(name, (Decision)decision.createStableCopy());
      addNode(decisionNode);

      decisions.get(name).add(decisionNode);
    }

    return decisions;
  }

  private Map<String, List<PageNode>> getPageNodes(final PageflowDefinition pageflowDefinition) {
    Map<String, List<PageNode>> pages = new HashMap<String, List<PageNode>>();

    for (Page page : pageflowDefinition.getPages()) {
      addPageNode(pages, page);
    }

    addPageNode(pages, pageflowDefinition.getStartPage());

    return pages;
  }

  private void addPageNode(final Map<String, List<PageNode>> pages, final PageElements page) {
    if (!DomUtil.hasXml(page)) return;

    String name = page.getName().getStringValue();

    if (StringUtil.isEmptyOrSpaces(name)) name = UNDEFINED_NAME;
    if (!pages.containsKey(name)) {
      pages.put(name, new ArrayList<PageNode>());
    }
    final PageNode pageNode = new PageNode(name, (PageElements)page.createStableCopy());
    pages.get(name).add(pageNode);

    addNode(pageNode);
  }

  @Nullable
  public PageflowDefinition getPageflowDefinition() {
    final PageflowModel model = getModel();
    if (model == null || model.getRoots().size() != 1) return null;

    return model.getRoots().get(0).getRootElement();
  }

  public PageflowModel getModel() {
    return PageflowDomModelManager.getInstance(myProject).getPageflowModel(myFile);
  }
}
