package com.intellij.seam.graph.renderers;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.BasicGraphNodeRenderer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.seam.graph.PageflowEdge;
import com.intellij.seam.graph.PageflowNode;

import javax.swing.*;
import java.awt.*;

/**
 * User: Sergey.Vasiliev
 */
public class DefaultPageflowNodeRenderer extends BasicGraphNodeRenderer<PageflowNode, PageflowEdge> {

  public DefaultPageflowNodeRenderer(GraphBuilder<PageflowNode, PageflowEdge> builder) {
    super(builder, ModificationTracker.EVER_CHANGED);
  }

  protected JComponent getPresenationComponent(final String text) {

    return super.getPresenationComponent(text);
  }

  protected Icon getIcon(final PageflowNode node) {
    return node.getIcon();
  }

  protected String getNodeName(final PageflowNode node) {
    return node.getName();
  }

  protected Color getBackground(final PageflowNode node) {
    return Color.LIGHT_GRAY;
  }

  protected int getSelectionBorderWidth() {
    return 1;
  }
}
