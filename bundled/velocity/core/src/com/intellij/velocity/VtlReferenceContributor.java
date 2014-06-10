package com.intellij.velocity;

import com.intellij.openapi.util.TextRange;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import com.intellij.patterns.PsiElementPattern;
import static com.intellij.patterns.StandardPatterns.string;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.velocity.psi.PsiUtil;
import com.intellij.velocity.psi.VtlImplicitVariable;
import com.intellij.velocity.psi.files.VtlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Alexey Chmutov
 */
public class VtlReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PsiComment> VTLVARIABLE_COMMENT = psiElement(PsiComment.class).inFile(psiElement(VtlFile.class)).withText(string().contains(VtlFile.VTLVARIABLE_MARKER));
    public static final PsiElementPattern.Capture<PsiComment> VTLMACROLIBRARY_COMMENT = psiElement(PsiComment.class).inFile(psiElement(VtlFile.class)).withText(string().contains(VtlFile.VTLMACROLIBRARY_MARKER));
    public static final PsiElementPattern.Capture<PsiComment> VELOCITY_PROPERTIES_COMMENT = psiElement(PsiComment.class).inFile(psiElement(VtlFile.class)).withText(string().contains(VtlFile.VELOCITY_PROPERTIES_MARKER));

    public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
        registerImplicitVariableProvider(registrar);
        registerExternalMacroLibraryProvider(registrar);
        registerVelocityPropertiesProvider(registrar);
    }

    private void registerImplicitVariableProvider(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(VTLVARIABLE_COMMENT, new PsiReferenceProvider() {
            @NotNull
            public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
                return getReferencesToJavaTypes(element);
            }
        });

        registrar.registerReferenceProvider(VTLVARIABLE_COMMENT, new PsiReferenceProvider() {
            @NotNull
            public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
                final String text = element.getText();
                TextRange range = PsiUtil.findRange(text, "name=\"", "\"");
                if (range == null) {
                    return PsiReference.EMPTY_ARRAY;
                }

                final String name = range.substring(text);
                final VtlImplicitVariable variable = ((VtlFile) element.getContainingFile()).findImplicitVariable(name);
                if (variable == null) {
                    return PsiReference.EMPTY_ARRAY;
                }

                PsiReferenceBase<PsiComment> ref = new PsiReferenceBase<PsiComment>((PsiComment) element, TextRange.from(range.getStartOffset(), name.length())) {
                    public PsiElement resolve() {
                        return variable;
                    }

                    public Object[] getVariants() {
                        return EMPTY_ARRAY;
                    }
                };
                final List<PsiReference> result = new SmartList<PsiReference>();
                result.add(ref);
                findAndAddReferencesByElement(element, "file=\"", "\"", result);
                return result.toArray(PsiReference.EMPTY_ARRAY);
            }
        });
    }

    private void registerExternalMacroLibraryProvider(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(VTLMACROLIBRARY_COMMENT, new PsiReferenceProvider() {
            @NotNull
            public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
                final List<PsiReference> result = new SmartList<PsiReference>();
                if (findAndAddReferencesByElement(element, "path=\"", "\"", result)) {
                    findAndAddReferencesByElement(element, "file=\"", "\"", result);
                }
                return result.toArray(PsiReference.EMPTY_ARRAY);
            }
        });
    }

    private void registerVelocityPropertiesProvider(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(VELOCITY_PROPERTIES_COMMENT, new PsiReferenceProvider() {
            @NotNull
            public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
                final List<PsiReference> result = new SmartList<PsiReference>();
                if (findAndAddReferencesByElement(element, "path=\"", "\"", result)) {
                    findAndAddReferencesByElement(element, "runtime_root=\"", "\"", result);
                    findAndAddReferencesByElement(element, "file=\"", "\"", result);
                }
                return result.toArray(PsiReference.EMPTY_ARRAY);
            }
        });
    }

    private static boolean findAndAddReferencesByElement(@NotNull final PsiElement element, @NotNull String startMarker, @NotNull String endMarker, @NotNull Collection<PsiReference> collection) {
        final String text = element.getText();
        TextRange range = PsiUtil.findRange(text, startMarker, endMarker);
        if (range == null) {
            return false;
        }
        final String filePath = range.substring(text);
        FileReference[] fileReferences = PsiUtil.getFileReferences(filePath, element, range.getStartOffset(), false);
        return collection.addAll(Arrays.asList(fileReferences));
    }

    public static PsiReference[] getReferencesToJavaTypes(PsiElement element) {
        final String text = element.getText();
        TextRange range = findTypeNameRange(text);
        if (range == null) {
            return PsiReference.EMPTY_ARRAY;
        }
        final JavaClassReferenceProvider provider = new JavaClassReferenceProvider(element.getProject());
        return provider.getReferencesByString(range.substring(text), element, range.getStartOffset());
    }

    @Nullable
    public static TextRange findTypeNameRange(@NotNull String text) {
        return PsiUtil.findRange(text, "type=\"", "\"");
    }
}
