package com.intellij.seam.dependencies.renderers;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.BasicGraphNodeRenderer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.seam.dependencies.beans.SeamComponentNodeInfo;
import com.intellij.seam.dependencies.beans.SeamDependencyInfo;
import com.intellij.seam.dependencies.beans.SeamDomComponentNodeInfo;
import com.intellij.seam.dependencies.beans.UnknownBijectionNodeInfo;
import com.intellij.ui.LightColors;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;

public class DefaultSeamComponentRenderer extends BasicGraphNodeRenderer<SeamComponentNodeInfo, SeamDependencyInfo> {

  public DefaultSeamComponentRenderer(GraphBuilder<SeamComponentNodeInfo, SeamDependencyInfo> builder) {
    super(builder, ModificationTracker.EVER_CHANGED);
  }

  protected JComponent getPresenationComponent(final String text) {

    return super.getPresenationComponent(text);
  }

  protected Icon getIcon(final SeamComponentNodeInfo node) {
    return node.getIcon();
  }

  @NonNls
  protected String getNodeName(final SeamComponentNodeInfo node) {
    final String name = node.getName();

    return StringUtil.isEmptyOrSpaces(name) ? "Noname": name;
  }

  protected Color getBackground(final SeamComponentNodeInfo node) {
    if (node instanceof SeamDomComponentNodeInfo) return LightColors.BLUE;
    if (node instanceof UnknownBijectionNodeInfo) return LightColors.YELLOW;

    return Color.LIGHT_GRAY;
  }

  protected int getSelectionBorderWidth() {
    return 1;
  }
}
