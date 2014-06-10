/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

import com.intellij.openapi.graph.view.Arrow;
import com.intellij.openapi.graph.view.Drawable;
import com.intellij.openapi.graph.view.LineType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * @author Konstantin Bulenkov
 */
public interface UmlRelationship {
  LineType getLineType();

  Arrow getArrow();

  String getLabel();

  UmlRelationshipFactory FACTORY = new UmlRelationshipFactory() {
    public UmlRelationship createAggregation(String label) {
      return null;
    }

    public UmlRelationship createAssociation(String label) {
      return null; //TODO implement mew plz
    }

    public UmlRelationship createComposition(String label) {
      return null; //TODO implement mew plz
    }

    public UmlRelationship createDependency(final String label) {
      return new UmlRelationship(){
        public LineType getLineType() {return LineType.DASHED_1;}
        public Arrow getArrow() {return CREATE.getArrow();}
        public String getLabel() {return label;}
        public @NonNls String toString() {return "DEPENDENCY" + " " + label;}
      };
    }
  };

  UmlRelationship DEPENDENCY = new UmlRelationship(){
    public LineType getLineType() {return LineType.DASHED_1;}
    public Arrow getArrow() {return CREATE.getArrow();}
    public String getLabel() {return "";}
    public @NonNls String toString() {return "DEPENDENCY";}
  };

  @NonNls String ANGLE_ARROW = "angle";
  UmlRelationship CREATE = new UmlRelationship(){
    public LineType getLineType() {return LineType.DASHED_1;}
    public Arrow getArrow() {
        Arrow arrow = Arrow.Statics.getCustomArrow(ANGLE_ARROW);
        if(arrow == null) {
            GeneralPath shape = new GeneralPath();
            shape.moveTo(-8F, -5F);
            shape.lineTo(0.0F, 0.0F);
            shape.lineTo(-8F, 5F);
            arrow = Arrow.Statics.addCustomArrow(ANGLE_ARROW, shape, new Color(255, 255, 255, 0));
        }
        return arrow;
    }
    public @NonNls String getLabel() {return "<html>&laquo;create&raquo;</html>";}
    public @NonNls String toString() {return "CREATE";}
  };

  UmlRelationship TO_ONE = new UmlTwoForkedRelationship() {
    public LineType getLineType() {return LineType.LINE_1;}
    public Arrow getArrow() {return CREATE.getArrow();}
    public String getLabel() {return "1:1";}
    public Arrow getSourceArrow() {return Arrow.DIAMOND;}
    public @NonNls String toString() {return "TO_ONE";}
  };

  UmlRelationship TO_MANY = new UmlTwoForkedRelationship() {
    public LineType getLineType() {return LineType.LINE_1;}
    public Arrow getArrow() {return CREATE.getArrow();}
    public String getLabel() {return "1:*";}
    public Arrow getSourceArrow() {return Arrow.DIAMOND;}
    public @NonNls String toString() {return "TO_MANY";}
  };


  UmlRelationship GENERALIZATION = new UmlRelationship() {
    public LineType getLineType() {return LineType.LINE_1;}
    public Arrow getArrow() {return Arrow.DELTA;}
    public String getLabel() {return "";}
    public @NonNls String toString() {return "GENERALIZATION";}
  };

  UmlRelationship INTERFACE_GENERALIZATION = new UmlRelationship() {
    public LineType getLineType() {return LineType.LINE_1;}
    public Arrow getArrow() {return Arrow.DELTA;}
    public String getLabel() {return "";}
    public @NonNls String toString() {return "INTERFACE_GENERALIZATION";}
  };

  UmlRelationship REALIZATION = new UmlRelationship() {
    public LineType getLineType() {return LineType.DASHED_1;}
    public Arrow getArrow() {return Arrow.DELTA;}
    public String getLabel() {return "";}
    public @NonNls String toString() {return "REALIZATION";}
  };

  UmlRelationship NO_RELATIONSHIP = new UmlRelationship() {
    public @Nullable LineType getLineType() {return null;}
    public @Nullable Arrow getArrow() {return null;}
    public String getLabel() {return "";}
    public @NonNls String toString() {return "NO_RELATIONSHIP";}
  };

  UmlRelationship ANNOTATION = new UmlRelationship() {
    public LineType getLineType() {return LineType.DOTTED_2;}
    public Arrow getArrow() {return Arrow.NONE;}
    public String getLabel() {return "";}
    public @NonNls String toString() {return "ANNOTATION";}
  };

  UmlRelationship INNER_CLASS = new UmlRelationship() {
    private @NonNls final String INNER_CLASS_ARROW = "InnerClassArrow";
    private static final int R = 5;
    public LineType getLineType() {
      return LineType.LINE_1;
    }

    public Arrow getArrow() {
      if (Arrow.Statics.getCustomArrow(INNER_CLASS_ARROW) == null) {
        Drawable arrow = new Drawable() {
          public void paint(final Graphics2D g) {
            Paint paint = g.getPaint();
            g.setPaint(g.getBackground());
            g.fillOval(-2*R, -R, 2*R, 2*R);
            g.setPaint(paint);
            g.drawOval(-2*R, -R, 2*R, 2*R);
            g.drawLine(-R, -R+2, -R, R-2);
            g.drawLine(-2*R+2, 0, -2, 0);
          }

          public Rectangle getBounds() {return new Rectangle(-R, -R, R, R);}
        };
        Arrow.Statics.addCustomArrow(INNER_CLASS_ARROW, arrow);
      }
      return Arrow.Statics.getCustomArrow(INNER_CLASS_ARROW);
    }
    public String getLabel() {return "";}
    public @NonNls String toString() {return "INNER_CLASS";}
  };

  interface UmlRelationshipFactory {
    UmlRelationship createAggregation(String label);
    UmlRelationship createAssociation(String label);
    UmlRelationship createComposition(String label);
    UmlRelationship createDependency(String label);
  }

  interface UmlTwoForkedRelationship extends UmlRelationship {
    Arrow getSourceArrow();
  }

  UmlRelationship[] KNOWN_RELATIONSIPS ={
    ANNOTATION,
    CREATE,
    DEPENDENCY,
    GENERALIZATION,
    INNER_CLASS,
    INTERFACE_GENERALIZATION,
    NO_RELATIONSHIP,
    REALIZATION,
    TO_MANY,
    TO_ONE
  };
}
