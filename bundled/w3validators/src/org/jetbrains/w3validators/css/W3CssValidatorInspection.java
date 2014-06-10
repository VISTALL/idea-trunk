package org.jetbrains.w3validators.css;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.css.*;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.containers.BidirectionalMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.w3validators.W3ValidatorsBundle;
import org.w3c.css.css.StyleSheet;
import org.w3c.css.css.StyleSheetParser;
import org.w3c.css.parser.CssError;
import org.w3c.css.parser.CssParseException;
import org.w3c.css.parser.Errors;
import org.w3c.css.parser.analyzer.ParseException;
import org.w3c.css.parser.analyzer.Token;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * NOTE: original W3C css-validator source files were changed in the following places:
 *
 * 1. org.w3c.css.util.Messages: all of the property files were renamed to Messages.lang.properties to be properly copied to out on compile
 * 2. org.w3c.css.values.CssColorCSS1: inherited "definedColors" field was renamed to "definedColors2" to avoid overloading
 * 3. org.w3c.css.values.CssColorCSS2: "definedColors" -> "definedColors2"
 * 4. CSS3Properties.properties: "background-color" property was changed to "org.w3c.css.properties.css1.CssBackgroundColor"
 * 5. org.w3c.css.util.Messages: adjustUrl should return non-modified URL (to enable properies loading from the jar)
 *
 * @author spleaner
 */
public class W3CssValidatorInspection extends LocalInspectionTool {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.w3validators.css.W3CssValidatorInspection");

  public String myCssVersion = "css3";

  @NonNls
  private static final BidirectionalMap<String, String> myVersionToProfile;
  static {
    myVersionToProfile = new BidirectionalMap<String, String>();
    myVersionToProfile.put("CSS Level 1", "css1");
    myVersionToProfile.put("CSS Level 2", "css2");
    myVersionToProfile.put("CSS Level 2.1", "css21");
    myVersionToProfile.put("CSS Level 3", "css3");
  }

  @NotNull
  public String getGroupDisplayName() {
    return W3ValidatorsBundle.message("css.inspection.family");
  }

  @NotNull
  public String getDisplayName() {
    return W3ValidatorsBundle.message("css.inspection.name");
  }

  @NotNull
  public String getShortName() {
    return "W3CssValidation";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Override
  public JComponent createOptionsPanel() {
    final JPanel result = new JPanel(new BorderLayout());

    final JPanel inner = new JPanel();
    inner.setBorder(BorderFactory.createTitledBorder("Settings"));
    inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
    result.add(inner, BorderLayout.NORTH);

    final JPanel versionPanel = new JPanel(new BorderLayout());
    versionPanel.add(new JLabel("CSS Version:"), BorderLayout.WEST);
    final JComboBox comboBox = new JComboBox(myVersionToProfile.keySet().toArray());

    final List<String> list = myVersionToProfile.getKeysByValue(myCssVersion);
    if (list != null && list.size() == 1) {
      comboBox.setSelectedItem(list.get(0));
    }

    comboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(final ItemEvent e) {
        final Object item = e.getItem();
        if (item instanceof String) {
          final String version = (String)item;
          final String temp = myVersionToProfile.get(version);
          if (!myCssVersion.equals(temp)) {
            myCssVersion = temp;
          }
        }
      }
    });

    versionPanel.add(comboBox, BorderLayout.CENTER);
    inner.add(versionPanel);
    
    return result;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new W3ValidatorVisitor(myCssVersion, holder, isOnTheFly);
  }

  private static class W3ValidatorVisitor extends CssElementVisitor {
    private final ProblemsHolder myHolder;
    private final boolean myOnTheFly;
    private final ApplContext myContext;

    public W3ValidatorVisitor(final String cssVersion, final ProblemsHolder holder, final boolean onTheFly) {
      myHolder = holder;
      myOnTheFly = onTheFly;

      myContext = new ApplContext(W3ValidatorsBundle.message("css.content.type"));
      myContext.setCssVersion(cssVersion);
    }

    @Override
    public void visitCssStylesheet(final CssStylesheet stylesheet) {
      ProgressManager.getInstance().checkCanceled();
      
      if (!stylesheet.isValid()) {
        return;
      }

      if (PsiUtilBase.getTemplateLanguageFile(stylesheet) != stylesheet.getContainingFile()) {
        return;
      }

      final String s = stylesheet.getText();
      final ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
      try {
        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(stylesheet.getProject());
        final Document document = documentManager.getDocument(stylesheet.getContainingFile());

        final PsiFile psiFile = stylesheet.getContainingFile();
        if (psiFile != null) {
          final VirtualFile virtualFile = psiFile.getVirtualFile();
          if (virtualFile != null) {
            final String url = virtualFile.getUrl();

            String protocol = VirtualFileManager.extractProtocol(url);
            if (LocalFileSystem.PROTOCOL.equals(protocol)) {
              final StyleSheetParser parser = new StyleSheetParser();
              parser.parseStyleElement(myContext, bais, null, "all", new URL(url), 0);

              final StyleSheet styleSheet = parser.getStyleSheet();
              styleSheet.findConflicts(myContext);

              final Errors errors = styleSheet.getErrors();
              reportErrors(stylesheet, document, errors);
            }
          }
        }
      }
      catch (MalformedURLException e) {
        LOG.error(e);
      }
      finally {
        try {
          bais.close();
        }
        catch (IOException e) {
        }
      }
    }

    private void reportErrors(final CssStylesheet stylesheet, final Document document, final Errors errors) {
      ProgressManager.getInstance().checkCanceled();
      
      final int startLine = document.getLineNumber(stylesheet.getTextRange().getStartOffset());
      final PsiFile psiFile = stylesheet.getContainingFile();

      for (CssError error : errors.getErrors()) {
        final int line = error.getLine() - 1;
        if (line > 0) {
          final int startOffset = document.getLineStartOffset(line + startLine);

          @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
          final Exception exception = error.getException();
          if (exception instanceof ParseException) {
            final ParseException parseException = (ParseException)exception;
            if (parseException instanceof CssParseException) {
              final String message = parseException.getMessage();
              if (message == null) {
                continue;
              }

              final CssParseException cssParseException = (CssParseException)parseException;
              final String property = cssParseException.getProperty();
              if (property != null) {
                PsiElement psiElement = psiFile.findElementAt(startOffset);
                if (psiElement instanceof PsiWhiteSpace) {
                  psiElement = psiFile.findElementAt(psiElement.getTextRange().getEndOffset());
                }

                if (psiElement != null) {
                  final PsiElement element = psiElement.getParent();
                  if (element instanceof CssDeclaration) {
                    final CssDeclaration declaration = (CssDeclaration)element;
                    myHolder.registerProblem(declaration, "W3C: " + message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                  }
                }
              }
              else {
                final Exception exception1 = cssParseException.getException();
                if (exception1 instanceof ParseException) {
                  final String message1 = exception1.getMessage();
                  if (message1 == null) {
                    continue;
                  }

                  final ParseException parseException1 = (ParseException)exception1;
                  final Token token = parseException1.currentToken;
                  if (token != null) {
                    final int tokenLine = token.beginLine - 1;
                    final int tokenColumn = token.beginColumn;

                    final int lineStartOffset = document.getLineStartOffset(tokenLine + startLine);
                    final PsiElement psiElement = psiFile.findElementAt(lineStartOffset + tokenColumn);

                    if (psiElement != null) {
                      myHolder.registerProblem(psiElement, "W3C: " + message1, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                  }
                }
              }
            }
            else if (parseException instanceof InvalidParamException) {
              PsiElement psiElement = psiFile.findElementAt(startOffset);
              if (psiElement instanceof PsiWhiteSpace) {
                psiElement = psiFile.findElementAt(psiElement.getTextRange().getEndOffset());
              }

              final String message = parseException.getMessage();
              if (message == null) {
                continue;
              }

              if (psiElement != null) {
                final PsiElement element = psiElement.getParent();
                if (element instanceof CssDeclaration) {
                  final CssDeclaration declaration = (CssDeclaration)element;
                  myHolder.registerProblem(declaration,  "W3C: " + message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
                else if (element instanceof CssSimpleSelector) {
                  final PsiElement parent = element.getParent();
                  if (parent != null && parent instanceof CssSelector) {
                    final PsiElement selectorList = parent.getParent();
                    if (selectorList != null) {
                      myHolder.registerProblem(selectorList,  "W3C: " + message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
