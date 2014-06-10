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

package com.intellij.beanValidation.toolWindow.tree.nodes;

import com.intellij.beanValidation.utils.BVUtils;
import com.intellij.psi.PsiMember;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.containers.SortedList;

import java.util.Collection;
import java.util.Comparator;

/**
 * @author Konstantin Bulenkov
 */
public abstract class AbstractBVNode extends SimpleNode {
  public AbstractBVNode(SimpleNode parent) {
    super(parent);
  }

  protected static Collection<PsiMemberSimpleNode> createSortedList() {
    return new SortedList<PsiMemberSimpleNode>(new Comparator<PsiMemberSimpleNode>() {
      public int compare(PsiMemberSimpleNode node, PsiMemberSimpleNode node2) {
        final PsiMember member = node.getMember();
        final PsiMember member2 = node2.getMember();

        if (member != null && member.isValid() && member2 != null && member2.isValid()) {
          final boolean inLib = BVUtils.isInLibrary(member);
          final boolean inLib2 = BVUtils.isInLibrary(member2);

          if (inLib != inLib2) {
            return inLib ? 1 : -1;
          }

          String name = member.getName();
          String name1 = member2.getName();

          if (name != null && name1 != null) {
            return name.compareTo(name1);
          }
        }

        return 0;
      }
    });
  }
}
