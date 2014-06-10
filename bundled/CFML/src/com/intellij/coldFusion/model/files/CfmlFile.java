package com.intellij.coldFusion.model.files;

import com.intellij.coldFusion.model.psi.CfmlImplicitVariable;
import com.intellij.coldFusion.model.psi.CfmlPsiUtil;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Factory;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lera Nikolaenko
 * Date: 24.02.2009
 */
public class CfmlFile extends PsiFileBase/* implements FileResolveScopeProvider*/ {
    private final CachedValue<Map<String, CfmlImplicitVariable>> myImplicitVars;
    @NonNls
    public static final String CFMLVARIABLE_MARKER = "@cfmlvariable ";
    @NonNls
    public static final String CFMLJAVALOADER_MARKER = "@javaloader ";

    @NonNls
    private static final Pattern IMPLICIT_VAR_DECL_PATTERN = Pattern.compile("<!---[ \\n\\r\\t\\f]*" + CFMLVARIABLE_MARKER + "name=\"([^\"]+)\" type=\"([^\"]*)\"" + "~(--->)" + "--->");

    CachedValueProvider<Map<String, CfmlImplicitVariable>> createImplicitVarsProvider() {
        return new CachedValueProvider<Map<String, CfmlImplicitVariable>>() {
            public CachedValueProvider.Result<Map<String, CfmlImplicitVariable>> compute() {
                final Map<String, CfmlImplicitVariable> result = new THashMap<String, CfmlImplicitVariable>();
                CfmlFile.this.accept(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitComment(final PsiComment comment) {
                        final String text = comment.getText();
                        final String[] nameAndType = CfmlFile.this.findVariableNameAndType(text);
                        if (nameAndType == null) {
                            return;
                        }
                        CfmlImplicitVariable var = ContainerUtil.getOrCreate(result, nameAndType[0], new Factory<CfmlImplicitVariable>() {
                            public CfmlImplicitVariable create() {
                                return new CfmlImplicitVariable(CfmlFile.this, comment, nameAndType[0]);
                            }
                        });
                        var.setType(nameAndType[1]);
                    }
                });
                return CachedValueProvider.Result.create(result, CfmlFile.this);
            }
        };
    }

    public CfmlFile(FileViewProvider viewProvider, @NotNull Language language) {
        super(viewProvider, language);
        myImplicitVars = getManager().getCachedValuesManager().createCachedValue(createImplicitVarsProvider(), false);
    }

    @NotNull
    public FileType getFileType() {
        return CfmlFileType.INSTANCE;
    }

    @NotNull
    public String getPresentableName() {
        return "CfmlFile:" + getName();
    }

    public String toString() {
        return getPresentableName();
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        if (!processExportableDeclarations(processor, state)) {
            return false;
        }
        return CfmlPsiUtil.processDeclarations(processor, state, lastParent, this);
    }

    private boolean processExportableDeclarations(PsiScopeProcessor processor, ResolveState state) {
        for (final CfmlImplicitVariable var : myImplicitVars.getValue().values()) {
            if (!processor.execute(var, state)) {
                return false;
            }
        }
        return true;
    }

    private String[] findVariableNameAndType(String text) {
        Pattern IMPLICIT_VAR_DECL_PATTERN_TEMP =
          Pattern.compile(new StringBuilder().append("<!---([^@]*)").append(CFMLVARIABLE_MARKER).append("name=\"([^\"]+)\" type=\"([^\"]*)(.*)").toString());
        Matcher matcher = IMPLICIT_VAR_DECL_PATTERN_TEMP.matcher(text);

      Pattern LOADER_DECL_PATTERN_TEMP =
        Pattern.compile(new StringBuilder().append("<!---([^@]*)").append(CFMLJAVALOADER_MARKER).append("[ \\n\\r\\t\\f]*name=\"([^\"]+)\"[ \\n\\r\\t\\f]*(jarPath=\"([^\"]+)\"[ \\n\\r\\t\\f]*).*").toString());
        Matcher javaLoaderMatcher = LOADER_DECL_PATTERN_TEMP.matcher(text);
        if (!matcher.matches()) {
          if (!javaLoaderMatcher.matches()) {
            return null;
          } else {
            return new String[]{javaLoaderMatcher.group(2), "javaloader"};
          }
        }
        return new String[]{matcher.group(2), matcher.group(3)};
    }

    @Nullable
    public CfmlImplicitVariable findImplicitVariable(String name) {
        return myImplicitVars.getValue().get(name);
    }

    /*
    public GlobalSearchScope getFileResolveScope() {
        return ProjectScope.getAllScope(getProject());
    }
    */
}
/*
public class CfmlFile extends PsiFileImpl implements PsiFile, FileResolveScopeProvider {
    public CfmlFile(@NotNull IElementType elementType, IElementType contentElementType, @NotNull FileViewProvider provider) {
        super(elementType, contentElementType, provider);
    }

    public CfmlFile(@NotNull FileViewProvider provider) {
        super(provider);
    }

    @NotNull
    public FileType getFileType() {
        return CfmlFileType.INSTANCE;
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        visitor.visitFile(this);
    }

    public GlobalSearchScope getFileResolveScope() {
        return ProjectScope.getAllScope(getProject());
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return CfmlLanguage.INSTANCE;
    }


}
*/
