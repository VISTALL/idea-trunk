/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
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

package org.jetbrains.plugins.ruby.support.utils;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLock;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.RepositoryElementsManager;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.parsing.ChameleonTransforming;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CharTable;

/**
 *
 */
@SuppressWarnings({"ConstantConditions"})
public class DebugUtil {
    private static final Logger LOG = Logger.getInstance(DebugUtil.class.getName());

    public static String psiTreeToString(PsiElement element, boolean
            skipWhitespaces) {
        return treeToString(SourceTreeToPsiMap.psiElementToTree(element),
                skipWhitespaces);
    }

    public static String treeToString(ASTNode root, boolean skipWhitespaces) {
        StringBuilder buffer = new StringBuilder();
        treeToBuffer(buffer, root, 0, skipWhitespaces, false, false);
        return buffer.toString();
    }

    public static String treeToString(ASTNode root, boolean
            skipWhitespaces, boolean showRanges) {
        StringBuilder buffer = new StringBuilder();
        treeToBuffer(buffer, root, 0, skipWhitespaces, showRanges, false);
        return buffer.toString();
    }


    public static String treeToStringWithUserData(TreeElement root,
                                                  boolean skipWhitespaces) {
        StringBuilder buffer = new StringBuilder();
        treeToBufferWithUserData(buffer, root, 0, skipWhitespaces);
        return buffer.toString();
    }

    public static String treeToStringWithUserData(PsiElement root,
                                                  boolean skipWhitespaces) {
        StringBuilder buffer = new StringBuilder();
        treeToBufferWithUserData(buffer, root, 0, skipWhitespaces);
        return buffer.toString();
    }

//TODO: remove this method. Necessary for debug purposes while we do not have PsiElement's for javascript
    public static PsiElement treeElementToPsi(ASTNode element) {
        ProgressManager.getInstance().checkCanceled();

        if (element == null) return null;

        if (element instanceof PsiElement) {
            return (PsiElement) element;
        } else if (element instanceof RepositoryTreeElement) {
            return RepositoryElementsManager.getOrFindPsiElement((RepositoryTreeElement) element);
        } else {
            return element.getUserData(TreeElement.PSI_ELEMENT_KEY);
        }
    }

    public static void treeToBuffer(StringBuilder buffer,
                                    ASTNode root,
                                    int indent,
                                    boolean skipWhiteSpaces,
                                    boolean showRanges,
                                    final boolean showChildrenRanges) {
        if (skipWhiteSpaces && root.getElementType() ==
                ElementType.WHITE_SPACE) return;

        for (int i = 0; i < indent; i++) {
            buffer.append(' ');
        }
        if (root instanceof CompositeElement) {
            final PsiElement psiElement = root.getPsi();
            if (psiElement != null) {
                buffer.append(psiElement.toString());
            } else {
                buffer.append(root.getElementType().toString());
            }
        } else {
            String text = root.getText();
            text = StringUtil.replace(text, "\n", "\\n");
            text = StringUtil.replace(text, "\r", "\\r");
            text = StringUtil.replace(text, "\t", "\\t");
            buffer.append(root.toString());
            buffer.append("('");
            buffer.append(text);
            buffer.append("')");
        }
        if (showRanges) buffer.append(root.getTextRange());
        buffer.append("\n");
        if (root instanceof CompositeElement) {
            ChameleonTransforming.transformChildren(root);
            ASTNode child = root.getFirstChildNode();

            if (child == null) {
                for (int i = 0; i < indent + 2; i++) {
                    buffer.append(' ');
                }
                buffer.append("<empty list>\n");
            } else {
                while (child != null) {
                    treeToBuffer(buffer, child, indent + 2, skipWhiteSpaces,
                            showChildrenRanges, showChildrenRanges);
                    child = child.getTreeNext();
                }
            }
        }
    }

    private static void treeToBufferWithUserData(final StringBuilder buffer,
                                                 final TreeElement root, final int indent,
                                                 final boolean skipWhiteSpaces) {
        if (skipWhiteSpaces && root.getElementType() ==
                ElementType.WHITE_SPACE) return;

        for (int i = 0; i < indent; i++) {
            buffer.append(' ');
        }
        if (root instanceof CompositeElement) {
            buffer.append(SourceTreeToPsiMap.treeElementToPsi(root).toString());
        } else {
            String text = root.getText();
            text = StringUtil.replace(text, "\n", "\\n");
            text = StringUtil.replace(text, "\r", "\\r");
            text = StringUtil.replace(text, "\t", "\\t");
            buffer.append(root.toString());
            buffer.append("('");
            buffer.append(text);
            buffer.append("')");
        }
        buffer.append(root.getUserDataString());
        buffer.append("\n");
        if (root instanceof CompositeElement || ((LeafElement) root).isChameleon()) {
            PsiElement[] children =
                    SourceTreeToPsiMap.treeElementToPsi(root).getChildren();

            for (PsiElement child : children) {
                treeToBufferWithUserData(buffer,
                        (TreeElement) SourceTreeToPsiMap.psiElementToTree(child), indent + 2,
                        skipWhiteSpaces);
            }

            if (children.length == 0) {
                for (int i = 0; i < indent + 2; i++) {
                    buffer.append(' ');
                }
                buffer.append("<empty list>\n");
            }
        }
    }

    private static void treeToBufferWithUserData(StringBuilder buffer,
                                                 PsiElement root, int indent, boolean skipWhiteSpaces) {
        if (skipWhiteSpaces && root instanceof PsiWhiteSpace) return;

        for (int i = 0; i < indent; i++) {
            buffer.append(' ');
        }
        if (root instanceof CompositeElement) {
            buffer.append(root);
        } else {
            String text = root.getText();
            text = StringUtil.replace(text, "\n", "\\n");
            text = StringUtil.replace(text, "\r", "\\r");
            text = StringUtil.replace(text, "\t", "\\t");
            buffer.append(root.toString());
            buffer.append("('");
            buffer.append(text);
            buffer.append("')");
        }
        buffer.append(((UserDataHolderBase) root).getUserDataString());
        buffer.append("\n");

        PsiElement[] children = root.getChildren();

        for (PsiElement child : children) {
            treeToBufferWithUserData(buffer, child, indent + 2, skipWhiteSpaces);
        }

        if (children.length == 0) {
            for (int i = 0; i < indent + 2; i++) {
                buffer.append(' ');
            }
            buffer.append("<empty list>\n");
        }

    }

    public static void checkTreeStructure(ASTNode anyElement) {
        ASTNode root = anyElement;
        while (root.getTreeParent() != null) {
            root = root.getTreeParent();
        }
        if (root instanceof CompositeElement) {
            synchronized (PsiLock.LOCK) {
                checkSubtree(root);
            }
        }
    }

    private static void checkSubtree(ASTNode root) {
        if (root.getFirstChildNode() == null) {
            if (root.getLastChildNode() != null) {
                throw new IncorrectTreeStructureException(root, "firstChild == null, but lastChild != null");
            }
        } else {
            for (ASTNode child = root.getFirstChildNode(); child != null;
                 child = child.getTreeNext()) {
                if (child instanceof CompositeElement) {
                    checkSubtree(child);
                }
                if (child.getTreeParent() != root) {
                    throw new IncorrectTreeStructureException(child, "child has wrong parent value");
                }
                if (child == root.getFirstChildNode()) {
                    if (child.getTreePrev() != null) {
                        throw new IncorrectTreeStructureException(root,
                                "firstChild.prev != null");
                    }
                } else {
                    if (child.getTreePrev() == null) {
                        throw new IncorrectTreeStructureException(child, "not first child has prev == null");
                    }
                    if (child.getTreePrev().getTreeNext() != child) {
                        throw new IncorrectTreeStructureException(child,
                                "element.prev.next != element");
                    }
                }
                if (child.getTreeNext() == null) {
                    if (root.getLastChildNode() != child) {
                        throw new IncorrectTreeStructureException(child, "not last child has next == null");
                    }
                }
            }
        }
    }

    public static void checkParentChildConsistent(ASTNode element) {
        ASTNode treeParent = element.getTreeParent();
        if (treeParent == null) return;
        ASTNode[] elements = treeParent.getChildren(null);
        if (ArrayUtil.find(elements, element) == -1) {
            throw new IncorrectTreeStructureException(element, "child cannot be found among parents children");
        }
        //LOG.debug("checked consistence: "+System.identityHashCode(element));
    }

    public static void checkSameCharTabs(ASTNode element1, ASTNode element2) {
        final CharTable fromCharTab = SharedImplUtil.findCharTableByTree(element1);
        final CharTable toCharTab = SharedImplUtil.findCharTableByTree(element2);
        LOG.assertTrue(fromCharTab == toCharTab);
    }

    public static String psiToString(final PsiElement file, final boolean skipWhitespaces, final boolean showRanges) {
        final StringBuilder stringBuffer = new
                StringBuilder(file.getTextLength() * 5);
        if (file.getNode() == null) {
            psiToBuffer(stringBuffer, file, 0, skipWhitespaces, showRanges, false);
        } else {
            treeToBuffer(stringBuffer, file.getNode(), 0, skipWhitespaces,
                         showRanges, false);
        }
        return stringBuffer.toString();
    }

    public static void psiToBuffer(StringBuilder buffer,
                                   PsiElement root,
                                   int indent,
                                   boolean skipWhiteSpaces,
                                   boolean showRanges,
                                   final boolean showChildrenRanges) {
        if (skipWhiteSpaces && root instanceof PsiWhiteSpace) return;

        for (int i = 0; i < indent; i++) {
            buffer.append(' ');
        }
        final String rootStr = root.toString();
        buffer.append(rootStr);
        PsiElement child = root.getFirstChild();
        if (child == null) {
            String text = root.getText();
            text = StringUtil.replace(text, "\n", "\\n");
            text = StringUtil.replace(text, "\r", "\\r");
            text = StringUtil.replace(text, "\t", "\\t");
            buffer.append("('");
            buffer.append(text);
            buffer.append("')");
        }

        if (showRanges) buffer.append(root.getTextRange());
        buffer.append("\n");
        while (child != null) {
            psiToBuffer(buffer, child, indent + 2, skipWhiteSpaces,
                    showChildrenRanges, showChildrenRanges);
            child = child.getNextSibling();
        }
    }


    public static class IncorrectTreeStructureException extends RuntimeException {
        private final ASTNode myElement;

        public IncorrectTreeStructureException(ASTNode element, String message) {
            super(message);
            myElement = element;
        }

        public ASTNode getElement() {
            return myElement;
        }
    }
}
