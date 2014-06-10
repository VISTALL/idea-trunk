package com.intellij.seam.graph.renderers;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.BasicGraphNodeRenderer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.seam.graph.beans.BasicPagesNode;
import com.intellij.seam.graph.beans.BasicPagesEdge;
import com.intellij.seam.graph.beans.ExceptionNode;
import com.intellij.ui.LightColors;

import javax.swing.*;
import java.awt.*;

/**
 * User: Sergey.Vasiliev
 */
public class DefaultPagesNodeRenderer extends BasicGraphNodeRenderer<BasicPagesNode, BasicPagesEdge> {
  public DefaultPagesNodeRenderer(final GraphBuilder<BasicPagesNode, BasicPagesEdge> graphBuilder) {
    super(graphBuilder, ModificationTracker.EVER_CHANGED);
  }

  protected JComponent getPresenationComponent(final String text) {
    return super.getPresenationComponent(text);
  }

  protected Icon getIcon(final BasicPagesNode node) {
    return node.getIcon();
  }

  protected String getNodeName(final BasicPagesNode node) {
    return node.getName();
  }

  protected Color getBackground(final BasicPagesNode node) {
    return node instanceof ExceptionNode ?  Color.LIGHT_GRAY :LightColors.BLUE ;
  }

  protected int getSelectionBorderWidth() {
    return 1;
  }
}



