/*
 * Copyright 2005-2006 Olivier Descout
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
package org.intellij.idea.lang.javascript.intention;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl;
import com.intellij.lang.javascript.psi.impl.JSNewExpressionImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import org.intellij.idea.lang.javascript.psiutil.FindReferenceUtil;
import org.jetbrains.annotations.NonNls;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class JSFunctionVisitor extends JSElementVisitor {

    @Override public void visitElement(PsiElement element) {
        if (element instanceof JSFunction) {
            this.visitJSFunctionDeclaration((JSFunction) element);
        } else if (element instanceof JSReferenceExpression) {
            if (element == element.getParent().getChildren()[0]) {
                final PsiElement firstChild = element.getParent().getFirstChild();

                if (firstChild != null && firstChild.toString().indexOf(JSTokenTypes.NEW_KEYWORD.toString()) >= 0) {
                    this.visitJSNewExpression(new JSNewExpressionImpl(element.getParent().getNode()));
                }
            }
        }
        else if (element instanceof XmlFile) {
            processFile((XmlFile)element);
        }

        super.visitElement(element);

        for (final PsiElement childElement : element.getChildren()) {
            childElement.accept(this);
        }
    }

    @Override public void visitJSNewExpression(JSNewExpression newExpression) {}

    private static final Logger logger = Logger.getInstance(JSFunctionVisitor.class.getName());

    private void processFile(XmlFile file) {
      int  count = 0;
      long start = System.currentTimeMillis();

      for (final JSElement element : getEmbeddedJSElements(file)) {
          this.visitElement(element);
          count++;
      }
      logger.info("Processed inspection " + this.getClass().getSimpleName() + " on file " + file.getName() +
                  " (" + count + " blocks) in " + (System.currentTimeMillis() - start) + " ms.");
    }

    static Iterable<JSElement> getEmbeddedJSElements(final XmlFile file) {
        return new Iterable<JSElement>() {
                public Iterator<JSElement> iterator() {
                    return new EmbeddedJSElementIterator(file);
                }
            };
    }

    /**
     * This iterator makes a textual search for every SCRIPT sub-text and
     * tries and finds JSElement instances within this SCRIPT, on which
     * this.visitElement() can be called.
     */
    private static class EmbeddedJSElementIterator implements Iterator<JSElement> {
        @NonNls private static final String SCRIPT_START = "<SCRIPT";
        @NonNls private static final String SCRIPT_END   = "</SCRIPT>";
        @NonNls private static final String HTML_TAG_END = ">";

        final XmlFile file;
        final String          fileText;
        int                   scriptStartIndex;
        int                   scriptEndIndex;
        JSElement             next;

        public EmbeddedJSElementIterator(XmlFile file) {
            this.file           = file;
            this.fileText       = file.getText();
            this.scriptEndIndex = -SCRIPT_END.length();

            this.findNext();
        }

        public boolean hasNext() {
            return (this.next != null);
        }

        public JSElement next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }

            JSElement next = this.next;

            this.findNext();
            return next;
        }

        private void findNext() {
            if (!(this.next == null || this.next instanceof JSEmbeddedContentImpl)) {
                this.next = PsiTreeUtil.getNextSiblingOfType(this.next, JSElement.class);

                if (this.next != null) {
                    return;
                }
            }

            PsiElement psiElement;

            do {
                if (this.scriptStartIndex >= 0) {
                    this.scriptStartIndex = this.fileText.indexOf(SCRIPT_START, this.scriptEndIndex + SCRIPT_END.length());
                }

                if (this.scriptStartIndex < 0) {
                    this.next = null;
                    return;
                }

                int offset = ((this.scriptStartIndex >= 0) ? this.fileText.indexOf(HTML_TAG_END, this.scriptStartIndex + SCRIPT_START.length()) + 1
                              : 0);

                this.scriptEndIndex = ((offset > 0) ? this.fileText.indexOf(SCRIPT_END, offset) : -1);
                if (this.scriptEndIndex < 0) {
                    this.scriptStartIndex = -1;
                    return;
                }

                psiElement = this.file.findElementAt(offset);

                if (psiElement instanceof PsiWhiteSpace) {
                    psiElement = PsiTreeUtil.getNextSiblingOfType(psiElement, JSElement.class);
                }

                if (psiElement != null && psiElement instanceof JSElement) {
                    this.next = FindReferenceUtil.getFarthestAncestor(psiElement, JSElement.class);
                }
            } while (this.next == null);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
