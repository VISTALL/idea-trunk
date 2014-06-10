/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.grails.perspectives.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsBundle;

/**
 * User: Dmitry.Krasilschikov
 * Date: 06.08.2007
 */
public class DomainClassRelationsInfo {
//  @Nullable
//  public String getVarNameBackward() {
//    return myVarNameBackward;
//  }
//
//  public void setVarNameBackward(@Nullable String varNameBackward) {
//    myVarNameBackward = varNameBackward;
//  }

  public enum Relation {
    UNKNOWN,
    BELONGS_TO,
    HAS_MANY,
    STRONG,
    DOUBLESTRONG,
    MANY_TO_MANY
  }

  public static final String UNKNOWN_NAME = "unknown";
  public static final String BELONGS_TO_NAME = "belongsTo";
  public static final String HAS_MANY_NAME = "hasMany";
  public static final String TRANSIENTS_NAME = "transients";
  public static final String CONSTRAINTS_NAME = "constraints";

  private final DomainClassNode mySource;
  private final DomainClassNode myTarget;
  private Relation myRelation;

  @Nullable
  private String myVarName;

//  @Nullable
//  private String myVarNameBackward;

  public DomainClassRelationsInfo(DomainClassNode source, DomainClassNode target, Relation relation) {
    mySource = source;
    myTarget = target;
    myRelation = relation;
  }

  @NotNull
  public DomainClassNode getSource() {
    return mySource;
  }

  @NotNull
  public DomainClassNode getTarget() {
    return myTarget;
  }

  public Relation getRelation() {
    return myRelation;
  }

  public void setRelation(Relation relation) {
    myRelation = relation;
  }

  @NotNull
  public String getEdgeLabel() {
    String label = "";

    switch (myRelation) {
      case UNKNOWN:
      case DOUBLESTRONG: {
        label = GrailsBundle.message("domain.classes.relations.strong.strong");
        break;
      }
      case STRONG: {
        label = GrailsBundle.message("domain.classes.relations.strong");
        break;
      }
      case BELONGS_TO: {
        label = GrailsBundle.message("domain.classes.relations.belongs.to");
        break;
      }
      case HAS_MANY: {
        label = GrailsBundle.message("domain.classes.relations.has.many");
        break;
      }
      case MANY_TO_MANY: {
        label = GrailsBundle.message("domain.classes.relations.has.many.to.many");
        break;
      }
    }

    return label;
  }

  public void setVarName(@Nullable String varName) {
    this.myVarName = varName;
  }

  public boolean equals(final Object otherDomainClssRelatiionInfo) {
    if (this == otherDomainClssRelatiionInfo) return true;
    if (otherDomainClssRelatiionInfo == null || getClass() != otherDomainClssRelatiionInfo.getClass()) return false;

    final DomainClassRelationsInfo that = (DomainClassRelationsInfo) otherDomainClssRelatiionInfo;

    if (myRelation != that.getRelation()) return false;
    if (!mySource.equals(that.mySource)) return false;
    if (!myTarget.equals(that.myTarget)) return false;
    if ((myVarName != null) && !myVarName.equals(that.myVarName)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = mySource.hashCode();
    result = 31 * result + myTarget.hashCode();
    result = 31 * result + myRelation.hashCode();
    return result;
  }

  @Nullable
  public String getVarName() {
    return myVarName;
  }
}