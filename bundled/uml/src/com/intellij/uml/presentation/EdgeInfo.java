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

package com.intellij.uml.presentation;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.geom.YPoint;
import com.intellij.openapi.graph.layout.EdgeLayout;
import com.intellij.openapi.util.Pair;
import com.intellij.uml.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class EdgeInfo {
  private final String src;
  private final String trg;
  private final List<Pair<Double, Double>> points;
  public EdgeInfo(UmlEdge edge, GraphBuilder<UmlNode, UmlEdge> builder) {
    final UmlProvider provider = Utils.getProvider(builder);
    final UmlVfsResolver resolver = provider.getVfsResolver();

    src = resolver.getQualifiedName(edge.getSource().getIdentifyingElement());
    trg = resolver.getQualifiedName(edge.getTarget().getIdentifyingElement());
    final EdgeLayout edgeLayout = builder.getGraph().getEdgeLayout(builder.getEdge(edge));
    final YPoint sp = edgeLayout.getSourcePoint();
    points = new ArrayList<Pair<Double, Double>>();
    points.add(new Pair<Double, Double>(sp.getX(), sp.getY()));
    for (int i = 0; i < edgeLayout.pointCount(); i++) {
      final YPoint p = edgeLayout.getPoint(i);
      points.add(new Pair<Double, Double>(p.getX(), p.getY()));
    }
    final YPoint tp = edgeLayout.getTargetPoint();
    points.add(new Pair<Double, Double>(tp.getX(), tp.getY()));
  }

  public EdgeInfo(String src, String trg, List<Pair<Double, Double>> points) {
    this.src = src;
    this.trg = trg;
    this.points = points;
  }

  public String getSrc() {
    return src;
  }

  public String getTrg() {
    return trg;
  }

  public List<Pair<Double, Double>> getPoints() {
    return points;
  }
}