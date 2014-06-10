package org.jetbrains.plugins.grails.lang.gsp.formatter.settings;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.FileTypeIndentOptionsProvider;
import org.jetbrains.plugins.grails.fileType.GspFileType;

/**
 * @author ilyas
 */
public class GspIndentOptionsProvider implements FileTypeIndentOptionsProvider {
  public CodeStyleSettings.IndentOptions createIndentOptions() {
    final CodeStyleSettings.IndentOptions indentOptions = new CodeStyleSettings.IndentOptions();
    indentOptions.INDENT_SIZE = 2;
    return indentOptions;
  }

  public FileType getFileType() {
    return GspFileType.GSP_FILE_TYPE;
  }

  public IndentOptionsEditor createOptionsEditor() {
    return new SmartIndentOptionsEditor();
  }

  public String getPreviewText() {
    return "<html>\n" +
           "<head>\n" +
           "    <title>Welcome to Grails</title>\n" +
           "</head>\n" +
           "<body>\n" +
           "<g:each var=\"c\" in=\"${grailsApplication.controllerClasses}\">\n" +
           "    <li class=\"controller\"/>\n" +
           "</g:each>\n" +
           "</body>\n" +
           "</html>";
  }

  public void prepareForReformat(final PsiFile psiFile) {
  }
}