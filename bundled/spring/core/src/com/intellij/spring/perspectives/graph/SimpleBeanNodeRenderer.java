package com.intellij.spring.perspectives.graph;

import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.BasicNodeCellRenderer;
import com.intellij.openapi.graph.view.Graph2DView;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiClass;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class SimpleBeanNodeRenderer extends BasicNodeCellRenderer {
  final GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> myBuilder;

  public SimpleBeanNodeRenderer(@NotNull GraphBuilder<SpringBaseBeanPointer, SpringBeanDependencyInfo> builder,
                                ModificationTracker modificationTracker) {
    super(modificationTracker);
    myBuilder = builder;
  }

  protected JComponent getRendererComponent(final Graph2DView graph2DView,
                                            final NodeRealizer nodeRealizer,
                                            final Object object,
                                            final boolean b) {

    final JPanel wrapper = new JPanel();
    final Node node = nodeRealizer.getNode();
    final SpringBaseBeanPointer pointer = myBuilder.getNodeObject(node);
    if (pointer != null && pointer.isValid()) {
      String nodeName = pointer.getName();
      if (nodeName == null) nodeName = "Unknown";
      final PsiClass psiClass = pointer.getBeanClass();
      if (psiClass != null && nodeName.equals(psiClass.getQualifiedName())) nodeName = psiClass.getName();
      final String bugaga = ":-_()*.,?!@#$%^&|/\\<>";
      final List<String> wordsIn = NameUtil.nameToWordsLowerCase(nodeName);
      final StringBuffer sb = new StringBuffer("<html>");
      for (String s : wordsIn) {
        if (bugaga.contains(s)) continue;
        sb.append("<div align=\"center\">");
        sb.append(s);
        sb.append("</div>");
      }
      sb.append("</html>");
      final JLabel jLabel = new JLabel(sb.toString());

      wrapper.setBorder(new LineBorder(Color.BLACK));
      wrapper.setBackground(Color.WHITE);
      wrapper.add(jLabel, BorderLayout.CENTER);
    }
    return wrapper;
  }
}
