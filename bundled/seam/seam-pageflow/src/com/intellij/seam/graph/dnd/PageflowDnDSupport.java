package com.intellij.seam.graph.dnd;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.graph.builder.dnd.GraphDnDSupport;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.seam.PageflowIcons;
import com.intellij.seam.graph.PageflowDataModel;
import com.intellij.seam.graph.PageflowNode;
import com.intellij.seam.graph.PageflowNodeType;
import com.intellij.seam.graph.impl.*;
import com.intellij.seam.model.xml.pageflow.*;
import com.intellij.util.Function;
import com.intellij.util.xml.DomUtil;
import com.sun.tools.jdi.LinkedHashMap;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Sergey.Vasiliev
 */
public class PageflowDnDSupport implements GraphDnDSupport<PageflowNode, PageflowNodeType> {
  private final PageflowDataModel myDataModel;
  private static final String UNKNOWN = "unknown";

  public PageflowDnDSupport(final PageflowDataModel dataModel) {
    myDataModel = dataModel;
  }


  public Map<PageflowNodeType, Pair<String, Icon>> getDnDActions() {
    LinkedHashMap nodes = new LinkedHashMap();

    nodes.put(PageflowNodeType.START_STATE, new Pair<String, Icon>("Start State", PageflowIcons.PAGEFLOW_START));
    nodes.put(PageflowNodeType.START_PAGE, new Pair<String, Icon>("Start Page", PageflowIcons.PAGEFLOW_START_PAGE));
    nodes.put(PageflowNodeType.PAGE, new Pair<String, Icon>("Page", PageflowIcons.PAGEFLOW_PAGE));
    nodes.put(PageflowNodeType.DECISIION, new Pair<String, Icon>("Decision", PageflowIcons.PAGEFLOW_DECISION));
    nodes.put(PageflowNodeType.PROCESS_STATE, new Pair<String, Icon>("Process State", PageflowIcons.PAGEFLOW_PROCESS_STATE));
    nodes.put(PageflowNodeType.END_STATE, new Pair<String, Icon>("End State", PageflowIcons.PAGEFLOW_END_STATE));

    return nodes;
  }

  public boolean canStartDragging(final PageflowNodeType pageflowNodeType) {
    final PageflowDefinition pageflowDefinition = getDataModel().getPageflowDefinition();
    if (pageflowDefinition != null) {
      if (pageflowNodeType == PageflowNodeType.START_STATE) {
        return !DomUtil.hasXml(pageflowDefinition.getStartState());
      } else if (pageflowNodeType == PageflowNodeType.START_PAGE) {
        return !DomUtil.hasXml(pageflowDefinition.getStartPage());
      } 
    }
    return true;
  }

  public PageflowNode drop(final PageflowNodeType pageflowNodeType) {
    final PageflowDefinition pageflowDefinition = getDataModel().getPageflowDefinition();
    if (pageflowNodeType == PageflowNodeType.PAGE) {
      return startInWCA(getDataModel().getProject(), pageflowDefinition, getDropPageFunction());
    }

    switch (pageflowNodeType) {
      case PAGE:
        return startInWCA(getDataModel().getProject(), pageflowDefinition, getDropPageFunction());
      case DECISIION:
        return startInWCA(getDataModel().getProject(), pageflowDefinition, getDropDecisionFunction());
      case END_PAGE:
        break;
      case START_PAGE:
        return startInWCA(getDataModel().getProject(), pageflowDefinition, getDropStartPageFunction());
      case START_STATE:
        return startInWCA(getDataModel().getProject(), pageflowDefinition, getDropStartStateFunction());
      case PROCESS_STATE:
        return startInWCA(getDataModel().getProject(), pageflowDefinition, getDropProcessStateFunction());
      case END_STATE:
        return startInWCA(getDataModel().getProject(), pageflowDefinition, getDropEndStateFunction());
      default:
        break;
    }


    return null;
  }

  private static Function<PageflowDefinition, PageflowNode> getDropEndStateFunction() {
    return new Function<PageflowDefinition, PageflowNode>() {
      public PageflowNode fun(final PageflowDefinition pageflowDefinition) {
        final EndState endState = pageflowDefinition.addEndState();
        endState.getName().setStringValue(UNKNOWN);
        return new EndStateNode(UNKNOWN, (EndState)endState.createStableCopy());
      }
    };
  }

  private static Function<PageflowDefinition, PageflowNode> getDropStartStateFunction() {
    return new Function<PageflowDefinition, PageflowNode>() {
      public PageflowNode fun(final PageflowDefinition pageflowDefinition) {
        final StartState startState = pageflowDefinition.getStartState();
        startState.getName().setStringValue(UNKNOWN);
        return new StartStateNode(UNKNOWN, (StartState)startState.createStableCopy());
      }
    };
  }

  private static Function<PageflowDefinition, PageflowNode> getDropStartPageFunction() {
    return new Function<PageflowDefinition, PageflowNode>() {
      public PageflowNode fun(final PageflowDefinition pageflowDefinition) {
        final StartPage startPage = pageflowDefinition.getStartPage();
        startPage.getName().setStringValue(UNKNOWN);
        return new PageNode(UNKNOWN, (StartPage)startPage.createStableCopy());
      }
    };
  }

  private static Function<PageflowDefinition, PageflowNode> getDropProcessStateFunction() {
    return new Function<PageflowDefinition, PageflowNode>() {
      public PageflowNode fun(final PageflowDefinition pageflowDefinition) {
        final ProcessState processState = pageflowDefinition.addProcessState();
        processState.getName().setStringValue(UNKNOWN);
        return new ProcessStateNode(UNKNOWN, (ProcessState)processState.createStableCopy());
      }
    };
  }

  private static Function<PageflowDefinition, PageflowNode> getDropPageFunction() {
    return new Function<PageflowDefinition, PageflowNode>() {
      public PageflowNode fun(final PageflowDefinition pageflowDefinition) {
        final Page page = pageflowDefinition.addPage();
        page.getName().setStringValue(UNKNOWN);
        return new PageNode(UNKNOWN, (Page)page.createStableCopy());
      }
    };
  }

  private static Function<PageflowDefinition, PageflowNode> getDropDecisionFunction() {
    return new Function<PageflowDefinition, PageflowNode>() {
      public PageflowNode fun(final PageflowDefinition pageflowDefinition) {
        final Decision decision = pageflowDefinition.addDecision();
        decision.getName().setStringValue(UNKNOWN);
        return new DecisionNode(UNKNOWN, (Decision)decision.createStableCopy());
      }
    };
  }

  private static PageflowNode startInWCA(final Project project,
                                         final PageflowDefinition pageflowDefinition,
                                         final Function<PageflowDefinition, PageflowNode> function) {
    return new WriteCommandAction<PageflowNode>(project, DomUtil.getFile(pageflowDefinition)) {
      protected void run(final Result<PageflowNode> result) throws Throwable {
        result.setResult(function.fun(pageflowDefinition));
      }
    }.execute().getResultObject();

  }

  public PageflowDataModel getDataModel() {
    return myDataModel;
  }


  public List<String> getExistedNodesNames() {
    List<String> names = new ArrayList<String>();
    for (PageflowNode node : myDataModel.getNodes(false)) {
      final String s = node.getName();
      if (!StringUtil.isEmptyOrSpaces(s)) {
        names.add(s);
      }
    }
    return names;
  }
}
