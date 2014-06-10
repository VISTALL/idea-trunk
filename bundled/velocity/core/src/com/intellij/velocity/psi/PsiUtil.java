package com.intellij.velocity.psi;

import com.intellij.codeInsight.completion.util.PsiTypeCanonicalLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.velocity.psi.directives.VtlAssignment;
import com.intellij.velocity.psi.directives.VtlParse;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.files.VtlFileType;
import com.intellij.velocity.psi.reference.SoftFileReferenceSet;
import com.intellij.velocity.psi.reference.VtlFileReferenceSet;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Alexey Chmutov
 */
public class PsiUtil {
    @NonNls
    private static final String NULL_TYPE_NAME = "???";
    private static final Pattern FULLY_QUALIFIED_NAME_PATTERN = Pattern.compile("(([a-zA-Z_$][a-zA-Z_0-9$]*\\.)*)([a-zA-Z_$][a-zA-Z_0-9$]*)");

    public static VtlReferenceExpression createVtlReferenceExpression(final String text, final Project project) {
        final VtlFile dummyFile = createDummyFile(project, "$" + text);
        final PsiElement interpolation = dummyFile.getFirstChild();
        final PsiElement refStart = interpolation.getFirstChild();
        PsiElement ref = refStart.getNextSibling();
        assert ref instanceof VtlReferenceExpression;
        return (VtlReferenceExpression) ref;
    }

    public static PsiElement createIdentifierElement(Project project, String name) {
        final VtlFile dummyFile = createDummyFile(project, "$" + name);
        final PsiElement interpolation = dummyFile.getFirstChild();
        final PsiElement reference = interpolation.getLastChild();
        return assertElementType(reference.getFirstChild(), VtlElementTypes.IDENTIFIER);
    }

    private static PsiElement assertElementType(PsiElement element, VtlTokenType type) {
        assert element != null;
        ASTNode node = element.getNode();
        assert node != null && node.getElementType() == type;
        return element;
    }

    public static VtlLiteralExpressionType.VtlStringLiteral createStringLiteral(Project project, String text) {
        final VtlFile dummyFile = createDummyFile(project, "#m(" + text + ")");
        final PsiElement macroCall = dummyFile.getFirstChild();
        final PsiElement rightParen = macroCall.getLastChild();
        final PsiElement argList = rightParen.getPrevSibling();
        final PsiElement stringLiteral = argList.getFirstChild();
        assert stringLiteral instanceof VtlLiteralExpressionType.VtlStringLiteral;
        return (VtlLiteralExpressionType.VtlStringLiteral) stringLiteral;
    }

    public static VtlFile createDummyFile(Project project, String text) {
        final String fileName = "dummy." + VtlFileType.INSTANCE.getDefaultExtension();
        return (VtlFile) PsiFileFactory.getInstance(project).createFileFromText(fileName, VtlLanguage.INSTANCE, text);
    }

    public static boolean processDeclarations(@NotNull final PsiScopeProcessor processor, @NotNull final ResolveState state, @Nullable final PsiElement lastParent,
                                              @Nullable Set<PsiFile> filesVisited, @NotNull final PsiElement elementToProcess) {
        PsiElement child = lastParent == null ? elementToProcess.getLastChild() : lastParent.getPrevSibling();

        if (child == null || child.getParent() instanceof VtlDirectiveHeader) {
            return true;
        }
        do {
            if (child instanceof VtlNamedElement && !processor.execute(child, state)) {
                return false;
            }
            if (child instanceof VtlAssignment) {
                VtlVariable assignedVariable = ((VtlAssignment) child).getAssignedVariable();
                if (assignedVariable != null && !processor.execute(assignedVariable, state)) {
                    return false;
                }
            }
            if (child instanceof VtlParse) {
                if (filesVisited == null) {
                    filesVisited = new THashSet<PsiFile>();
                }
                VtlFile file = ((VtlParse) child).resolveFile();
                if (file != null && !filesVisited.contains(file)) {
                    filesVisited.add(file);
                    if (!processDeclarations(processor, ResolveState.initial(), null, filesVisited, file)) {
                        return false;
                    }
                }
            }
            child = child.getPrevSibling();
        } while (child != null);
        return true;
    }

    public static boolean isFormalNotationStart(@Nullable PsiElement element) {
        if (element == null) {
            return false;
        }
        String childText = element.getText().trim();
        return "${".equals(childText) || "$!{".equals(childText);
    }

    @NotNull
    public static String getPresentableText(@Nullable final PsiType psiType) {
        return psiType == null ? NULL_TYPE_NAME : psiType.getPresentableText();
    }

    @NotNull
    public static String getUnqualifiedName(@Nullable final String typeName) {
        if (typeName == null) {
            return NULL_TYPE_NAME;
        }
        return FULLY_QUALIFIED_NAME_PATTERN.matcher(typeName).replaceAll("$3");
    }

    @Nullable
    public static PsiType getBoxedType(PsiType type, @NotNull PsiElement context) {
        if (!(type instanceof PsiPrimitiveType) || PsiType.VOID.equals(type)) {
            return type;
        }
      PsiClassType boxedType = ((PsiPrimitiveType)type).getBoxedType(context);
        if (boxedType == null) {
            return type;
        }
        return boxedType;
    }

    @NotNull
    public static PsiSubstitutor getSuperClassSubstitutor(@NotNull PsiClass superClass, @NotNull PsiClassType classType) {
        final PsiClassType.ClassResolveResult classResolveResult = classType.resolveGenerics();
        return TypeConversionUtil.getSuperClassSubstitutor(superClass, classResolveResult.getElement(), classResolveResult.getSubstitutor());
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

    public static boolean isTypeOf(final PsiElement element, final VtlTokenType... elementTypes) {
        if (element == null) {
            return false;
        }
        PsiElement child = element.getFirstChild();
        if (child == null) {
            return false;
        }
        final IElementType childType = child.getNode().getElementType();
        for (VtlTokenType elementType : elementTypes) {
            if (childType == elementType) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static FileReference[] getFileReferences(@NotNull String text, @NotNull PsiElement element, int startOffset, final boolean considerVelocityProperties) {
        FileReferenceSet set = considerVelocityProperties
                ? new VtlFileReferenceSet(text, element, startOffset)
                : new SoftFileReferenceSet(text, element, startOffset);
        return set.getAllReferences();
    }

    @Nullable
    public static <T extends PsiFile> T findFile(@NotNull PsiReference[] references, Class<T> fileClass) {
        for (final PsiReference reference : references) {
            final PsiElement target = reference.resolve();
            if (fileClass.isInstance(target)) {
                return (T) target;
            }
        }
        return null;
    }

    @Nullable
    public static String getRelativePath(@NotNull PsiFile base, @NotNull PsiFile target) {
        return getRelativePath(getPath(base), getPath(target));
    }

    @Nullable
    public static String getRelativePath(@Nullable String basePath, @Nullable String targetPath) {
        if (basePath == null || targetPath == null) {
            return null;
        }
        String relativePath = FileUtil.getRelativePath(new File(basePath), new File(targetPath));
        if (relativePath == null) {
            return null;
        }
        return FileUtil.toSystemIndependentName(relativePath);
    }

    @Nullable
    public static String getPath(@NotNull PsiFile file) {
        final VirtualFile baseFile = file.getVirtualFile();
        if (baseFile == null) {
            return null;
        }
        return baseFile.getPath();
    }

    public static LookupElement createPsiTypeLookupElement(final PsiElement element, final String typeName) {
        return ApplicationManager.getApplication().runReadAction(new Computable<LookupElement>() {
            public LookupElement compute() {
                try {
                    final PsiElementFactory elementFactory = JavaPsiFacade.getInstance(element.getProject()).getElementFactory();
                    return new PsiTypeCanonicalLookupElement(elementFactory.createTypeFromText(typeName, element));
                } catch (IncorrectOperationException e) {
                    return null;
                }
            }
        });
    }
}
