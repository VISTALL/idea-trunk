package com.intellij.coldFusion.model.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Lera Nikolaenko
 * Date: 12.02.2009
 */
public class CfmlPsiUtil {
    @Nullable
    public static Collection<String> findBetween(@NotNull String source, @NotNull String startMarker, @NotNull String endMarker) {
        int fromIndex = 0;
        Collection<String> collection = new LinkedList<String>();
        while (fromIndex < source.length() && fromIndex >= 0) {
          int start = source.indexOf(startMarker, fromIndex);
          if (start < 0) {
              break;
          }
          start += startMarker.length();
          final int end = source.indexOf(endMarker, start);
          if (end < start) {
            break;
          }
          collection.add(source.substring(start, end));
          fromIndex = end + endMarker.length();
        }
        return collection;
    }

    @Nullable
    public static TextRange findRange(@NotNull String source, @NotNull String startMarker, @NotNull String endMarker) {
        int start = source.indexOf(startMarker);
        if (start < 0) {
            return null;
        }
        start += startMarker.length();
        final int end = source.indexOf(endMarker, start);
        if (end < start) {
            return null;
        }
        return new TextRange(start, end);
    }

    public static boolean processDeclarations(@NotNull final PsiScopeProcessor processor, @NotNull final ResolveState state, @Nullable final PsiElement lastParent,
                                              @NotNull final PsiElement elementToProcess) {
        PsiElement child = lastParent == null ? elementToProcess.getLastChild() : lastParent.getPrevSibling();

        if (child == null) {
            return true;
        }
        do {
            PsiElement element = child;
            if (child instanceof CfmlTag && "cfscript".equals(((CfmlTag)child).getName())) {
                ((CfmlTag)child).processDeclarations(processor, state, null, child);
            }
            if (child instanceof CfmlTag && ((CfmlTag)child).isDeclarativeInside()) {
                element = ((CfmlTag)child).getDeclarativeElement();
            }
            if (element instanceof /*CfmlNamedElement*/ PsiNamedElement && !processor.execute(element, state)) {
                return false;
            }
            if (element instanceof CfmlAssignment) {
                CfmlVariable assignedVariable = ((CfmlAssignment) element).getAssignedVariable();
                if (assignedVariable != null && !processor.execute(assignedVariable, state)) {
                    return false;
                }
            }
            if (element.getNode().getElementType() == CfscriptElementTypes.ARGUMENTS_LIST) {
              element.processDeclarations(processor, state, null, child);
            }
            child = child.getPrevSibling();
        } while (child != null);
        return true;
    }
    /*



    public CfmlPsiUtil INSTANCE = new CfmlPsiUtil();
    private static final CfmlWrapper<PsiElement> CfmlTrivialWrapper = new CfmlWrapper<PsiElement>() {
        public PsiElement getElement(PsiElement element) {
            return element;
        }
    };
    private static final CfmlWrapper<String> CfmlNameGetterWrapper = new CfmlWrapper<String>() {
        public String getElement(PsiElement element) {
            return element instanceof PsiNamedElement ? ((PsiNamedElement) element).getName() : "";
        }
    };

    private CfmlPsiUtil() {
    }

    public static Set<String> getFunctionsNameDefinedBefore(PsiFile file, int endOffset) {
        return getElementsFromFile(file, new CfmlFilter() {
            public boolean accept(PsiElement element) {
                return element instanceof CfmlFunctionDefinition;
            }
        }, CfmlNameGetterWrapper, endOffset);
    }

    public static Set<String> getFunctionsNamesDefined(PsiFile file) {
        return getFunctionsNameDefinedBefore(file, file.getTextLength());
    }

    public static Set<String> getFunctionDefinitionBefore(final PsiFile file, final String name, final int endOffset) {
        return getElementsFromFile(file, new CfmlFilter() {
            public boolean accept(PsiElement element) {
                return (element instanceof CfmlFunctionDefinition) &&
                        ((PsiNamedElement) element).getName().toLowerCase().equals(name.toLowerCase());
            }
        }, CfmlNameGetterWrapper, endOffset);
    }

    public static Set<PsiElement> resolveFunction(final PsiFile file, final String name, int endOffset) {
        return getElementsFromFile(file, new CfmlFilter() {
            public boolean accept(PsiElement element) {
                return (element instanceof CfmlFunctionDefinition) &&
                        ((PsiNamedElement) element).getName().toLowerCase().equals(name.toLowerCase());
            }
        }, CfmlTrivialWrapper, endOffset);
    }

    public static Set<PsiElement> resolveVariable(final PsiFile file, final String name) {
        return getElementsFromFile(file, new CfmlFilter() {
            public boolean accept(PsiElement element) {
                return (element instanceof CfmlVariableDefinition) &&
                        ((PsiNamedElement) element).getName().toLowerCase().equals(name.toLowerCase());
            }
        }, CfmlTrivialWrapper, file.getTextLength());
    }

    public static Set<String> getVariablesNamesDefined(PsiFile file) {
        return getVariablesNamesDefinedBefore(file, file.getTextLength());
    }

    public static Set<String> getVariablesNamesDefinedBefore(PsiFile file, int offset) {
        return getElementsFromFile(file, new CfmlFilter() {
            public boolean accept(PsiElement element) {
                return element instanceof CfmlVariableDefinition;
            }
        }, CfmlNameGetterWrapper, offset);
    }

    private interface CfmlWrapper<T> {
        T getElement(PsiElement element);
    }

    private static <T> void getElementsFromFileBeforePosition(Set<T> names, PsiElement element, CfmlFilter filter, CfmlWrapper<T> wrapper, int endOffset) {
        for (PsiElement child : element.getChildren()) {
            if (child.getTextRange().getStartOffset() >= endOffset) {
                break;
            }
            if (child instanceof PsiNamedElement && filter.accept(child)) {
                names.add(wrapper.getElement(child));
            } else {
                getElementsFromFileBeforePosition(names, child, filter, wrapper, endOffset);
            }
        }
    }

    private static <T> Set<T> getElementsFromFile(PsiFile file, CfmlFilter filter, CfmlWrapper<T> wrapper,
                                                  int endOffset) {
        Set<T> functionDefined = new LinkedHashSet<T>();
        getElementsFromFileBeforePosition(functionDefined, file, filter, wrapper, endOffset);
        return functionDefined;
    }
    */
}
