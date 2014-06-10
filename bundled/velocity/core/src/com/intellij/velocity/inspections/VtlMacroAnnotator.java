package com.intellij.velocity.inspections;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.BaseScopeProcessor;
import static com.intellij.velocity.VelocityBundle.message;
import com.intellij.velocity.psi.VtlElementTypes;
import com.intellij.velocity.psi.VtlExpression;
import com.intellij.velocity.psi.VtlMacro;
import com.intellij.velocity.psi.VtlParameterDeclaration;
import com.intellij.velocity.psi.directives.VtlMacroImpl;
import com.intellij.velocity.psi.directives.VtlParse;
import com.intellij.velocity.psi.files.VtlFile;
import org.jetbrains.annotations.NonNls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexey Chmutov
 */
public class VtlMacroAnnotator implements Annotator {
    @NonNls
    private static final String V_IDENT = "([a-zA-Z_][a-zA-Z_0-9-]*)";
    @NonNls
    private static final Pattern WOULD_BE_MACRO_CALL_PATTERN = Pattern.compile("(#" + V_IDENT + ")|(#\\{" + V_IDENT + "\\})");

    public void annotate(final PsiElement element, final AnnotationHolder holder) {
        if (element instanceof VtlMacroImpl) {
            final VtlMacroImpl macro = (VtlMacroImpl) element;
            final VtlFile file = macro.getContainingFile();
            String macroName = macro.getName();
            if (macroName != null && file.getNumberOfMacros(macroName) > 1) {
                holder.createErrorAnnotation(macro.getNameElementRange(), message("macro.is.already.defined", macroName, file.getName()));
            }
        } else if (element instanceof VtlParameterDeclaration) {
            VtlParameterDeclaration param = (VtlParameterDeclaration) element;
            String paramName = param.getName();
            if (paramName == null) {
                return;
            }
            PsiElement sibling = param.getPrevSibling();
            while (sibling != null) {
                if (sibling instanceof VtlParameterDeclaration
                        && paramName.equals(((VtlParameterDeclaration) sibling).getName())) {
                    final String msg = message("duplicated.parameter.name", paramName);
                    holder.createErrorAnnotation(param.getTextRange(), msg);
                    holder.createErrorAnnotation(sibling.getTextRange(), msg);
                }
                sibling = sibling.getPrevSibling();
            }
        } else if (element instanceof VtlParse) {
            final VtlParse parse = (VtlParse) element;
            VtlFile parsedFile = parse.resolveFile();
            if (parsedFile == null) {
                return;
            }
            final VtlExpression parsedFileElement = parse.getArgumentList().getArguments()[0];
            final VtlFile containingFile = parse.getContainingFile();
            for (String macroName : containingFile.getDefinedMacroNames()) {
                if (parsedFile.getNumberOfMacros(macroName) > 0) {
                    final String msg = message("macro.declaration.will.be.ignored", macroName, containingFile.getName(), parsedFile.getName());
                    holder.createWarningAnnotation(parsedFileElement, msg);
                }
            }
        } else {
            final ASTNode node = element.getNode();
            if (node == null || node.getElementType() != VtlElementTypes.TEMPLATE_TEXT) {
                return;
            }
            final Matcher matcher = WOULD_BE_MACRO_CALL_PATTERN.matcher(node.getText());
            if (!matcher.find()) {
                return;
            }
            final PsiFile file = element.getContainingFile();
            if (!(file instanceof VtlFile)) {
                return;
            }
            final VtlFile vtlFile = (VtlFile) file;
            do {
                int index = matcher.start(2) != -1 ? 2 : 4;
                final String macroName = matcher.group(index);
                final BaseScopeProcessor processor = new BaseScopeProcessor() {
                    public boolean execute(PsiElement element, ResolveState state) {
                        return !(element instanceof VtlMacro && macroName.equals(((VtlMacro) element).getName()));
                    }
                };
                if (!vtlFile.processAllMacrosInScope(processor, ResolveState.initial())) {
                    TextRange range = new TextRange(matcher.start(), matcher.end()).shiftRight(element.getTextOffset());
                    holder.createErrorAnnotation(range, message("will.be.considered.as.macro.call"));
                }
            } while (matcher.find());
        }
    }
}